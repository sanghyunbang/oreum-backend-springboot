package com.oreum.goods.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreum.goods.dao.GoodsOrderDAO;
import com.oreum.goods.dto.GoodsOrderDTO;
import com.oreum.goods.dto.IamportCallbackDTO;
import com.oreum.goods.dto.OrderItemDTO;
import com.oreum.goods.service.IamportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goods/iamport")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class IamportController {

    @Autowired
    private IamportService iamportService;

    @Autowired
    private GoodsOrderDAO goodsOrderDAO;

    // 아임포트 웹훅 콜백 (서버 간 통신)
    @PostMapping("/webhook")
    @Transactional
    public ResponseEntity<String> iamportWebhook(@RequestBody IamportCallbackDTO webhook) {
        try {
            String impUid = webhook.getImp_uid();
            String merchantUid = webhook.getMerchant_uid();
            
            System.out.println("Webhook received - imp_uid: " + impUid + ", merchant_uid: " + merchantUid);
            
            // 결제 검증
            boolean verificationResult = iamportService.verifyPayment(impUid, merchantUid);
            
            if (verificationResult) {
                // 주문 상태를 결제완료로 업데이트
                iamportService.updateOrderStatus(merchantUid, impUid, "결제완료");
                System.out.println("Payment verified and order status updated for: " + merchantUid);
                return ResponseEntity.ok("Webhook processed successfully");
            } else {
                iamportService.updateOrderStatus(merchantUid, impUid, "결제실패");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed");
            }
        } catch (Exception e) {
            System.err.println("Webhook processing error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing error");
        }
    }

    // 클라이언트에서 결제 검증 요청
    @PostMapping("/verify")
    @Transactional
    public ResponseEntity<Map<String, Object>> verifyPaymentFromClient(@RequestBody Map<String, String> payload) {
        String impUid = payload.get("imp_uid");
        String merchantUid = payload.get("merchant_uid");

        if (impUid == null || merchantUid == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false, 
                "message", "imp_uid 또는 merchant_uid가 누락되었습니다."
            ));
        }

        try {
            System.out.println("Client verification request - imp_uid: " + impUid + ", merchant_uid: " + merchantUid);
            
            // 결제 검증
            boolean verificationResult = iamportService.verifyPayment(impUid, merchantUid);
            
            if (verificationResult) {
                // 아임포트에서 결제 정보 조회하여 custom_data 추출
                String customData = iamportService.getCustomDataFromPayment(impUid);
                
                if (customData != null && !customData.isEmpty()) {
                    // custom_data로 주문 생성
                    processOrderFromCustomData(customData, impUid, merchantUid);
                }
                
                Integer amount = iamportService.getAmountFromPayment(impUid);
                return ResponseEntity.ok(Map.of(
                    "success", true, 
                    "message", "결제 검증 및 주문 처리 완료", 
                    "amount", amount
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false, 
                    "message", "결제 검증 실패"
                ));
            }
        } catch (Exception e) {
            System.err.println("Client payment verification error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false, 
                "message", "결제 검증 중 서버 오류 발생: " + e.getMessage()
            ));
        }
    }

    // custom_data로부터 주문 생성
    @Transactional
    private void processOrderFromCustomData(String customData, String impUid, String merchantUid) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> customDataMap = objectMapper.readValue(customData, Map.class);
            
            // orderDTO 파싱
            Map<String, Object> orderDTOMap = (Map<String, Object>) customDataMap.get("orderDTO");
            if (orderDTOMap == null) {
                throw new RuntimeException("orderDTO not found in custom_data");
            }
            
            GoodsOrderDTO orderDTO = new GoodsOrderDTO();
            orderDTO.setUserId((Integer) orderDTOMap.get("userId"));
            orderDTO.setAddressname((String) orderDTOMap.get("addressname"));
            orderDTO.setAddressnumber((String) orderDTOMap.get("addressnumber"));
            orderDTO.setZipcode((String) orderDTOMap.get("zipcode"));
            orderDTO.setAddressbasic((String) orderDTOMap.get("addressbasic"));
            orderDTO.setAddressdetail((String) orderDTOMap.get("addressdetail"));
            orderDTO.setRequest((String) orderDTOMap.get("request"));
            orderDTO.setPoint((Integer) orderDTOMap.get("point"));
            orderDTO.setTotal((Integer) orderDTOMap.get("total"));
            orderDTO.setStatus("결제완료");
            orderDTO.setImp_uid(impUid);
            orderDTO.setMerchant_uid(merchantUid);
            
            // 주문 생성
            goodsOrderDAO.addOrder(orderDTO);
            int orderId = orderDTO.getOrder_id();
            
            // 주문 상품 파싱
            List<Map<String, Object>> itemsToOrderList = (List<Map<String, Object>>) customDataMap.get("itemsToOrder");
            if (itemsToOrderList != null && !itemsToOrderList.isEmpty()) {
                List<OrderItemDTO> orderItems = itemsToOrderList.stream().map(itemMap -> {
                    OrderItemDTO item = new OrderItemDTO();
                    item.setOrder_id(orderId);
                    item.setGoods_options_id((Integer) itemMap.get("goods_options_id"));
                    item.setQty((Integer) itemMap.get("qty"));
                    item.setItem_price((Integer) itemMap.get("item_price"));
                    return item;
                }).toList();
                
                goodsOrderDAO.addItemOrder(orderItems);
            }
            
            // 포인트 사용 처리
            if (orderDTO.getPoint() > 0) {
                goodsOrderDAO.updatePoints(orderDTO);
            }
            
            System.out.println("Order created successfully - order_id: " + orderId + ", merchant_uid: " + merchantUid);
            
        } catch (Exception e) {
            System.err.println("Error processing order from custom_data: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to process order from custom_data", e);
        }
    }
}