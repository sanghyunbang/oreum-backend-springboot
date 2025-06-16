package com.oreum.goods.controller;

import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.oreum.goods.dao.GoodsOrderDAO;
import com.oreum.goods.dao.goodsDAO;
import com.oreum.goods.dto.goodsDTO;

@RestController
@RequestMapping("/api/goods")
@CrossOrigin(origins="http://localhost:3000",allowCredentials = "true")
public class GoodsController {
	
	@Autowired goodsDAO gdao;
	@Autowired GoodsOrderDAO goDAO;
	
	@GetMapping("/listAll")
	public List<goodsDTO> doListAll() {
	    return gdao.findAllGoods();
	}
	
	@GetMapping("/detailList")
	public goodsDTO doDetailList(@RequestParam("id") int id){
		goodsDTO fgoods = gdao.findGoods(id).get(0);
		return fgoods;
	}
	@GetMapping("/detailListOpt")
	public goodsDTO doDetailListOpt(@RequestParam("id") int id){
		goodsDTO fgoods = gdao.findGoodsOptions(id).get(0);
		return fgoods;
	}
	
	@PostMapping("/cartAdd")
	public String doCartAdd(@RequestBody Map<String,String> req) {
		if (goDAO.existsGoods(Integer.parseInt(req.get("userId")), Integer.parseInt(req.get("option")))) {
		    throw new IllegalStateException("해당 상품이 이미 장바구니에 존재합니다.");
		}
//		goodsDTO aGoods = goDAO.addGoods();
		return "";
	}
}
