package com.oreum.goods.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CartRequest {
	private int userId;
    private List<CartItem> options = new ArrayList<>();;

    @Data
    public static class CartItem {
        private int id;   // goods_option_id
        private int qty;
    }
}
