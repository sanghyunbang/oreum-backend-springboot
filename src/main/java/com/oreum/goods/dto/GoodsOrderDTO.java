package com.oreum.goods.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GoodsOrderDTO {
	private int cart_id;
	private int user_id;
	private int goods_option_id;
	private int qty;
	private LocalDateTime added_at;
}
