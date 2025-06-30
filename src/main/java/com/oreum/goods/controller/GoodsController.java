package com.oreum.goods.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreum.external.S3.S3Service;
import com.oreum.goods.dao.GoodsCartDAO;
import com.oreum.goods.dao.GoodsLikeDAO;
import com.oreum.goods.dao.GoodsOptionDAO;
import com.oreum.goods.dao.GoodsOrderDAO;
import com.oreum.goods.dao.ReviewDAO;
import com.oreum.goods.dao.goodsDAO;
import com.oreum.goods.dto.CartRequest;
import com.oreum.goods.dto.GoodsCartDTO;
import com.oreum.goods.dto.GoodsLikedDTO;
import com.oreum.goods.dto.GoodsOrderDTO;
import com.oreum.goods.dto.OrderItemDTO;
import com.oreum.goods.dto.ReviewDTO;
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
	@Autowired ReviewDAO rDAO;
	@Autowired S3Service s3Service;
	
	//상품
	@PostMapping(value = "/insert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<goodsDTO> insertGoods(
	    @RequestPart("goods") goodsDTO dto,
	    @RequestPart(value = "media", required = false) List<MultipartFile> mediaFiles
	) {
	    if (mediaFiles != null && !mediaFiles.isEmpty()) {
	        List<String> urls = new ArrayList<>();
	        for (MultipartFile file : mediaFiles) {
	            String url = s3Service.uploadFile(file);
	            urls.add(url);
	        }

	        // ✅ 이미지 URL 배열을 JSON 문자열로 저장
	        ObjectMapper mapper = new ObjectMapper();
	        try {
	            dto.setImg(mapper.writeValueAsString(urls));
	        } catch (JsonProcessingException e) {
	            return ResponseEntity.status(500).body(null);
	        }
	    }

	    gDAO.insertGoods(dto);
	    return ResponseEntity.ok(dto);
	}
	@PostMapping("/addGoodsItem")
	public ResponseEntity<String> addGoodsItem(@RequestBody List<goodsOptionDTO> options) {
		System.out.println(options);
	    goptDAO.insertGoodsOptions(options);
	    return ResponseEntity.ok("success");
	}
	@GetMapping("/listAll")
	public List<goodsDTO> doListAll() {
	    return gDAO.findAllGoods();
	}
	@GetMapping("/itemListAll")
	public List<goodsOptionDTO> doItemListAll(){
		return goptDAO.findAllItemGoods();
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
		System.out.println(fcart);
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
	            return "0";
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
	        return "1"; // 새로 추가된 항목이 있음
	    } else {
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
	    gcDAO.selRemoveCart(cartIds); // 이렇게 하면 MyBatis가 list로 인식함
	    return "1";
	}
	@PostMapping("/deleteCart")
	public String doDeleteCart(@RequestBody Map<String, List<Integer>> req) {
		List<Integer> cartIds = req.get("id");
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
	    System.out.println("odto: "+odto);

	    goDAO.addOrder(odto);
	    goDAO.updatePoints(odto);  //결제 포인트 차감
	    return ResponseEntity.ok(odto.getOrder_id());  // ✅ order_id 반환
	}
	@PostMapping("addOrderItem")
	public void addOrderItem(@RequestBody Map<String, List<OrderItemDTO>> req) {
	    List<OrderItemDTO> items = req.get("items");
	    goDAO.addItemOrder(items); // 주문 등록

	    // 하나씩 수량 차감
	    for (OrderItemDTO item : items) {
	        goptDAO.updateQtyOne(item);
	    }
	}
	@PostMapping("/deliveryList")
	public List<GoodsOrderDTO> doOrderList(@RequestBody Map<String,String> req){
		int userId = Integer.parseInt(req.get("userId"));
		List<GoodsOrderDTO> deliveryList = goDAO.findDeliveryList(userId);
		return deliveryList;
	}
	@PostMapping("/cancelOrder")
	public String selListOrder(@RequestBody Map<String,String> req) {
		int id = Integer.parseInt(req.get("order_id"));
		goDAO.cancelOrder(id);
		return "1";
	}
	@PostMapping("/deleteOrder")
	public void deleteOrder(@RequestBody Map<String,String> req) {
		int id = Integer.parseInt(req.get("order_id"));
		goDAO.deleteOrder(id);
	}
	
	@Scheduled(fixedRate = 600000) // 10분마다 실행
    public void updateOrderStatusByTime() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 결제완료 → 10분 후 배송중
        goDAO.updateToShipping(now.minusMinutes(10));

        // 2. 배송중 → 10분 후 배송완료
        goDAO.updateToDelivered(now.minusMinutes(10));
    }
	
	//리뷰 기능
	@GetMapping("/listReview")
	public List<ReviewDTO> doListReview(@RequestParam("id") int id) {
		List<ReviewDTO> rdto = rDAO.selectReview(id);
		System.out.println("rdto: "+rdto);
		return rdto;
	}
	@PostMapping("/addReview")
	public void doAddReview(@RequestBody Map<String,String> req) {
		String imageUrl = req.get("imageUrl");
		System.out.println("imageUrl: "+imageUrl);
		
		ReviewDTO dto = new ReviewDTO();
		dto.setUserId(Integer.parseInt(req.get("id")));
        dto.setOrderItemId(Integer.parseInt(req.get("orderItemId")));
        dto.setOrderId(Integer.parseInt(req.get("orderId")));
        dto.setRating(Integer.parseInt(req.get("rating")));
        dto.setContent(req.get("review"));
        if(req.get("imageUrl")==null) {
        	dto.setImageUrl("");
        	rDAO.insertReview(dto);
        }else {
        	dto.setImageUrl(req.get("imageUrl"));
            rDAO.insertReview(dto);
        }
        goDAO.updateReview(Integer.parseInt(req.get("orderItemId")));		//리뷰 작성 여부 확인
        goDAO.addPoints(Integer.parseInt(req.get("id")),75);	//리뷰 작성시 75포인트 추가
	}
	
	
	//좋아요
	@PostMapping("/likedList")
	public  List<GoodsLikedDTO> doListLiked(@RequestBody Map<String,String> req) {
		System.out.println("유저아이디: "+req.get("userId"));
		List<GoodsLikedDTO> glDTO = likeDAO.listLiked(Integer.parseInt(req.get("userId")));
		return glDTO;
	}
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
    
    //포인트
    @PostMapping("/getUserPoints")
    public String doGetUserPoints(@RequestBody Map<String, String> req) {
    	int userId = Integer.parseInt(req.get("userId"));
    	return goDAO.getUserPoints(userId);
    }
    
    

//    @PostMapping("/upload")
//    public ResponseEntity<List<String>> uploadImages(@RequestParam("images") List<MultipartFile> files) {
//        List<String> imagePaths = new ArrayList<>();
//        String uploadDir = "C:/upload/img/";	// 실제 저장 경로
//
//        for (MultipartFile file : files) {
//            if (!file.isEmpty()) {
//                try {
//                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//                    Path path = Paths.get(uploadDir + fileName);
//
////                    Files.createDirectories(path.getParent()); // 폴더 없으면 생성
//                    Files.write(path, file.getBytes());
//
//                    imagePaths.add("/img/" + fileName);
//                } catch (IOException e) {
//                    e.printStackTrace(); // ❗ 콘솔에 에러 출력 확인
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//                }
//            }
//        }
//
//        return ResponseEntity.ok(imagePaths); // 응답 꼭 돌려줘야 함
//    }

}
