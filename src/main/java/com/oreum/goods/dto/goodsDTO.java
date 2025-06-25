package com.oreum.goods.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class goodsDTO {
	private int id;
    private String name;
    private String category;
    private String brand;
    private int price;
    private int salePercent;
    private int likes;
    private String description;
    private String img;
    private String status = "판매중";
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
