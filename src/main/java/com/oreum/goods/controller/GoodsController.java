package com.oreum.goods.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oreum.goods.dao.GoodsCartDAO;
import com.oreum.goods.dao.GoodsLikeDAO;
import com.oreum.goods.dao.GoodsOptionDAO;
import com.oreum.goods.dao.GoodsOrderDAO;
import com.oreum.goods.dao.goodsDAO;
import com.oreum.goods.dto.CartRequest;
import com.oreum.goods.dto.GoodsCartDTO;
import com.oreum.goods.dto.GoodsOrderDTO;
import com.oreum.goods.dto.OrderItemDTO;
import com.oreum.goods.dto.goodsDTO;
import com.oreum.goods.dto.goodsOptionDTO;


@RestController
@RequestMapping("/api/goods")
@CrossOrigin(origins="http://localhost:3000",allowCredentials = "true")
@EnableScheduling
public class GoodsController {
	
	@Autowired goodsDAO gDAO;
	@Autowired GoodsOptionDAO goptDAO;
	@Autowired GoodsCartDAO gcDAO;
	@Autowired GoodsOrderDAO goDAO;
	@Autowired GoodsLikeDAO likeDAO;
	
	//굿즈
	@GetMapping("/listAll")
	public List<goodsDTO> doListAll() {
	    return gDAO.findAllGoods();
	}
	
	@GetMapping("/detailList")
	public List<goodsDTO> doDetailList(@RequestParam("id") int id){
		List<goodsDTO> fgoods = gDAO.findGoods(id);
		return fgoods;
	}
	@GetMapping("/detailListOpt")
	public List<goodsOptionDTO> doDetailListOpt(@RequestParam("id") int id){
		List<goodsOptionDTO> fgoodsOpt = goptDAO.findGoodsOptions(id);
		return fgoodsOpt;
	}
	
	
	//장바구니
	@PostMapping("/cartList")
	public List<GoodsCartDTO> doCartList(@RequestBody Map<String,String> req){
		String id = req.get("id");
		List<GoodsCartDTO> fcart = gcDAO.findUserCart(Integer.parseInt(id));
		return fcart;
	}
	@PostMapping("/cartAdd")
	public String doCartAdd(@RequestBody CartRequest req) {
	    int userId = req.getUserId();
	    List<Integer> duplicated = new ArrayList<>();
	    List<GoodsCartDTO> newItems = new ArrayList<>();

	    for (CartRequest.CartItem item : req.getOptions()) {
	        int optionId = item.getId();
	        Integer exCart = gcDAO.existsCart(userId, optionId);
	        if (exCart != null) {
	            duplicated.add(optionId); // 이미 존재하는 항목
	        } else {
	            GoodsCartDTO dto = new GoodsCartDTO();
	            dto.setUser_id(userId);
	            dto.setGoods_option_id(optionId);
	            dto.setQty(item.getQty());
	            dto.setAdded_at(LocalDateTime.now());
	            newItems.add(dto);
	        }
	    }
	    // 새로운 상품만 insert
	    for (GoodsCartDTO dto : newItems) {
	        gcDAO.addCart(dto);
	    }
	    if (!newItems.isEmpty()) {
	        System.out.println("1");
	        return "1"; // 새로 추가된 항목이 있음
	    } else {
	        System.out.println("0");
	        return "0"; // 모두 기존에 존재하던 항목
	    }
	}
	@PostMapping("/removeCart")
	public String doRemoveCart(@RequestBody Map<String,String> req) {
		int id = Integer.parseInt(req.get("id"));
		gcDAO.removeCart(id);
		return "1";
	}
	@PostMapping("/selRemoveCart")
	public String doSelRemoveCart(@RequestBody Map<String, List<Integer>> req) {
	    List<Integer> cartIds = req.get("id");
	    System.out.println("cartIds: "+cartIds);
	    gcDAO.selRemoveCart(cartIds); // 이렇게 하면 MyBatis가 list로 인식함
	    return "1";
	}
	@PostMapping("/deleteCart")
	public String doDeleteCart(@RequestBody Map<String, List<Integer>> req) {
		List<Integer> cartIds = req.get("id");
		System.out.println("cartIds: "+cartIds);
		gcDAO.selDeleteCart(cartIds);
		return "1";
	}
	
	
	//주문
	@PostMapping("/addOrder")
	public ResponseEntity<Integer> addOrder(@RequestBody GoodsOrderDTO order) {
	    GoodsOrderDTO odto = new GoodsOrderDTO();
	    odto.setUserId(order.getUserId());
	    odto.setAddressbasic(order.getAddressbasic());
	    odto.setAddressdetail(order.getAddressdetail());
	    odto.setAddressname(order.getAddressname());
	    odto.setAddressnumber(order.getAddressnumber());
	    odto.setZipcode(order.getZipcode());
	    odto.setRequest(order.getRequest());
	    odto.setPoint(order.getPoint());
	    odto.setTotal(order.getTotal());

	    goDAO.addOrder(odto);

	    return ResponseEntity.ok(odto.getOrder_id());  // ✅ order_id 반환
	}
	@PostMapping("addOrderItem")
	public void addOrderItem(@RequestBody Map<String, List<OrderItemDTO>> req) {
	    List<OrderItemDTO> items = req.get("items");
	    goDAO.addItemOrder(items); // ✅ 바로 리스트 전달
	}
	@PostMapping("/deliveryList")
	public List<GoodsOrderDTO> doOrderList(@RequestBody Map<String,String> req){
		int userId = Integer.parseInt(req.get("userId"));
		List<GoodsOrderDTO> deliveryList = goDAO.findDeliveryList(userId);
		return deliveryList;
	}
	
	@Scheduled(fixedRate = 60000) // 1분마다 실행
    public void updateOrderStatusByTime() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 결제완료 → 10분 후 배송중
        goDAO.updateToShipping(now.minusMinutes(1));

        // 2. 배송중 → 20분 후 배송완료
        goDAO.updateToDelivered(now.minusMinutes(1));
    }
	
	
	//좋아요
	@PostMapping("/liked")
    public ResponseEntity<?> like(@RequestBody Map<String, Integer> req) {
        int userId = req.get("userId");
        int goodsId = req.get("goodsId");

        boolean liked = likeDAO.existsLike(userId, goodsId);
        if (liked) {
            likeDAO.deleteLike(userId, goodsId);
            gDAO.decreaseLikes(userId, goodsId);
            return ResponseEntity.ok("unliked");
        } else {
            likeDAO.insertLike(userId, goodsId);
            gDAO.increaseLikes(userId, goodsId);
            return ResponseEntity.ok("liked");
        }
    }

    @PostMapping("/like/check")
    public boolean checkLike(@RequestBody Map<String, Integer> req) {
        return likeDAO.existsLike(req.get("userId"), req.get("goodsId"));
    }
}
