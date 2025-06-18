package com.oreum.posts.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PostLikeDTO {
	private int likeId;            // 좋아요 ID (PK)
    private int postId;            // 게시글 ID (FK)
    private int userId;            // 유저 ID (FK)
    private LocalDateTime createdAt;  // 좋아요 누른 시간

}
