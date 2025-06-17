package com.oreum.goods.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GoodsCartDTO {
    private int cart_id;
    private int user_id;
    private int goods_option_id; // 필요시 유지
    private int qty;
    private LocalDateTime added_at;

    // 옵션 정보
    private int option_id;
    private String option_name;
    private int stock_qty;

    // 상품 정보
    private int goods_id;
    private String goods_name;
    private int price;
    private int salePercent;
    private String img;
    private String brand;
    private String status;
}