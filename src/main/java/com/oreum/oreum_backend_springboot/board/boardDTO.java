package com.oreum.oreum_backend_springboot.board;

import lombok.Data;

@Data
public class boardDTO {
	
	private int postId;
    private int userId;
    private int boardId;
    private String type; // ENUM: "general", "curation"
    private String title;
    private String content;
    private String imageUrls;
    private int likeCount;
    private int commentCount;
    private boolean isDeleted;
    private java.sql.Timestamp createdAt;
    private java.sql.Timestamp updatedAt;
}
