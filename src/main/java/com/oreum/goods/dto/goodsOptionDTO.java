package com.oreum.goods.dto;

import lombok.Data;

@Data
public class goodsOptionDTO {
	private int id;
	private int goodsId;
	private String optionName;
	private int stockQty;
}
