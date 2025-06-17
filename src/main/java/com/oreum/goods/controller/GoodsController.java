package com.oreum.goods.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oreum.goods.dao.GoodsCartDAO;
import com.oreum.goods.dao.GoodsOptionDAO;
import com.oreum.goods.dao.GoodsOrderDAO;
import com.oreum.goods.dao.goodsDAO;
import com.oreum.goods.dto.CartRequest;
import com.oreum.goods.dto.GoodsCartDTO;
import com.oreum.goods.dto.goodsDTO;
import com.oreum.goods.dto.goodsOptionDTO;


@RestController
@RequestMapping("/api/goods")
@CrossOrigin(origins="http://localhost:3000",allowCredentials = "true")
public class GoodsController {
	
	@Autowired goodsDAO gDAO;
	@Autowired GoodsOptionDAO goptDAO;
	@Autowired GoodsCartDAO gcDAO;
	@Autowired GoodsOrderDAO goDAO;
	
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
	
	@PostMapping("/cartList")
	public List<GoodsCartDTO> doCartList(@RequestBody Map<String,String> req){
		String id = req.get("id");
		List<GoodsCartDTO> fcart = gcDAO.findUserCart(Integer.parseInt(id));
		return fcart;
	}
	@PostMapping("/cartAdd")
	public String doCartAdd(@RequestBody CartRequest req) {
	    int userId = req.getUserId();
	    for (CartRequest.CartItem item : req.getOptions()) {
	    	int optionId = item.getId();
	    	if (gcDAO.existsCart(userId, optionId)) {
	            return "0";
	        }
	    }
	    for (CartRequest.CartItem item : req.getOptions()) {
	        int optionId = item.getId();
	        int qty = item.getQty();
	        GoodsCartDTO dto = new GoodsCartDTO();
	        dto.setUser_id(userId);
	        dto.setGoods_option_id(optionId);
	        dto.setQty(qty);
	        dto.setAdded_at(LocalDateTime.now());
	        gcDAO.addCart(dto); // DAO에서 GoodsCartDTO를 받아서 insert
	    }
	    return "1";
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
}
