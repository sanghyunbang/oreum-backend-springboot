package com.oreum.goods.dto;

import lombok.Data;

@Data
public class ReviewDTO {
	private int userId;
    private int goodsId;
    private int orderId;
    private int rating;
    private String content;
    private String imageUrl; // S3 URL 또는 base64 미리보기 경로
}
