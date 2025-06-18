package com.oreum.goods.dto;

import java.util.List;

import lombok.Data;

@Data
public class GoodsOrderDTO {
		private int order_id;
	 	private int userId;
	    // 배송지 + 요청사항 + 포인트 전부 포함
	    private String addressname;
	    private String addressnumber;
	    private String zipcode;
	    private String addressbasic;
	    private String addressdetail;
	    private String request;
	    private int point;
	    private int total;

	    // 주문 상품들
	    private List<OrderItemDTO> items;
}
