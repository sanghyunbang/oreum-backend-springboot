package com.oreum.goods.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class ReviewDTO {
	//리뷰
	private int reviewId;
    private int userId;
    private int orderItemId;
    private int orderId;
    private int rating;
    private String content;
    private String imageUrl;
    //유저
    private String nickname;
    private String profileImage;
    private Timestamp createdAt;
    //상품
    private String goodsName;
    private String optionName;
    private int qty;
}
