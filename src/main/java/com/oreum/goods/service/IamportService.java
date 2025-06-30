package com.oreum.goods.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreum.goods.dao.GoodsOrderDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class IamportService {

    @Value("${iamport.api.key}") // application.properties에서 설정할 아임포트 API Key
    private String iamportApiKey;

    @Value("${iamport.api.secret}") // application.properties에서 설정할 아임포트 API Secret
    private String iamportApiSecret;

    @Autowired
    private GoodsOrderDAO goodsOrderDAO;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 아임포트 Access Token 발급
    private String getAccessToken() throws Exception {
        String url = "https://api.iamport.kr/users/getToken";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("imp_key", iamportApiKey);
        requestBody.put("imp_secret", iamportApiSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("response") && root.get("response").has("access_token")) {
                return root.get("response").get("access_token").asText();
            }
        }
        throw new Exception("Failed to get Iamport access token.");
    }

    // 아임포트 결제 정보 조회 (단일 건)
    private JsonNode getPaymentInfo(String impUid, String accessToken) throws Exception {
        String url = "https://api.iamport.kr/payments/" + impUid;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("response")) {
                return root.get("response");
            }
        }
        throw new Exception("Failed to get Iamport payment info for imp_uid: " + impUid);
    }

    // 결제 검증 로직
    public boolean verifyPayment(String impUid, String merchantUid) throws Exception {
        String accessToken = getAccessToken();
        JsonNode paymentInfo = getPaymentInfo(impUid, accessToken);

        // 아임포트에서 조회한 결제 상태가 'paid'인지 확인
        String status = paymentInfo.get("status").asText();
        if (!"paid".equals(status)) {
            System.err.println("Payment not paid. Status: " + status);
            return false;
        }

        // DB에 저장된 주문의 실제 금액과 아임포트에서 조회한 금액 비교
        Integer orderAmount = goodsOrderDAO.getAmountByMerchantUid(merchantUid);
        if (orderAmount == null) {
            System.err.println("Order not found for merchant_uid: " + merchantUid);
            return false;
        }

        Integer paidAmount = paymentInfo.get("amount").asInt();

        if (orderAmount.intValue() == paidAmount.intValue()) {
            System.out.println("Payment verification successful for merchant_uid: " + merchantUid);
            return true;
        } else {
            // 금액 불일치 시 결제 취소 (선택 사항)
            System.err.println("Payment amount mismatch. Order amount: " + orderAmount + ", Paid amount: " + paidAmount);
            // cancelPayment(impUid, accessToken, "금액 불일치"); // 필요 시 자동 취소 로직 추가
            return false;
        }
    }

    // 주문 상태 업데이트 (DB)
    public void updateOrderStatus(String merchantUid, String impUid, String status) {
        goodsOrderDAO.updateOrderStatusAndImpUid(merchantUid, impUid, status);
    }

    // 주문 상태만 업데이트 (DB) - imp_uid가 필요 없는 경우
    public void updateOrderStatus(String merchantUid, String status) {
        goodsOrderDAO.updateOrderStatus(merchantUid, status);
    }

    // DB에서 주문 금액 조회
    public Integer getAmountFromOrder(String merchantUid) {
        return goodsOrderDAO.getAmountByMerchantUid(merchantUid);
    }

    // DB에서 order_id 조회
    public Integer getOrderIdFromOrder(String merchantUid) {
        return goodsOrderDAO.getOrderIdByMerchantUid(merchantUid);
    }
 // 아임포트에서 결제 정보의 custom_data 조회
    public String getCustomDataFromPayment(String impUid) throws Exception {
        String accessToken = getAccessToken();
        JsonNode paymentInfo = getPaymentInfo(impUid, accessToken);
        
        if (paymentInfo.has("custom_data")) {
            JsonNode customDataNode = paymentInfo.get("custom_data");
            if (customDataNode.isTextual()) {
                return customDataNode.asText();
            } else {
                return customDataNode.toString();
            }
        }
        return null;
    }

    // 아임포트에서 결제 금액 조회
    public Integer getAmountFromPayment(String impUid) throws Exception {
        String accessToken = getAccessToken();
        JsonNode paymentInfo = getPaymentInfo(impUid, accessToken);
        
        if (paymentInfo.has("amount")) {
            return paymentInfo.get("amount").asInt();
        }
        return null;
    }
    // 아임포트 결제 취소 (선택 사항)
    public void cancelPayment(String impUid, String accessToken, String reason) throws Exception {
        String url = "https://api.iamport.kr/payments/cancel";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("imp_uid", impUid);
        requestBody.put("reason", reason);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            System.out.println("Payment cancelled successfully for imp_uid: " + impUid);
        } else {
            System.err.println("Failed to cancel payment for imp_uid: " + impUid + ". Response: " + response.getBody());
            throw new Exception("Failed to cancel Iamport payment.");
        }
    }
}
