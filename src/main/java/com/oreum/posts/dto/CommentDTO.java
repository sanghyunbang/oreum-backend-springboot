package com.oreum.posts.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentDTO {

	private int commentId;
    private int postId;
    private int userId;
    private Integer parentId;  // 대댓글일 경우 부모 댓글 ID, null 가능
    private String content;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String nickname;
}
