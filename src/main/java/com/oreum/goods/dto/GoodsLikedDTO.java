package com.oreum.goods.dto;

import lombok.Data;

@Data
public class GoodsLikedDTO {
	private int goodsId;
    private String name;
    private String category;
    private String brand;
    private int price;
    private int salePercent;
    private String description;
    private String img;
    private int likes;
    private String status;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;
    private java.sql.Timestamp likedAt; // 좋아요 누른 시간
}
