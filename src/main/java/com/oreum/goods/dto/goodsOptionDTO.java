package com.oreum.goods.dto;

import lombok.Data;

@Data
public class goodsOptionDTO {
	private int id;
	private int goods_id;
	private String option_name;
	private int stock_qty;
}
