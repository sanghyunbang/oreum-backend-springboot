package com.oreum.goods.dto;

import lombok.Data;

@Data
public class OrderItemDTO {
	private int order_item_id;       // 주문 항목 ID
    private int order_id;            // 주문 ID (Foreign Key)
    private int goods_options_id;    // 상품 옵션 ID (Foreign Key)
    private int qty;                 // 수량
    private int item_price;          // 개별 또는 총 가격
}
