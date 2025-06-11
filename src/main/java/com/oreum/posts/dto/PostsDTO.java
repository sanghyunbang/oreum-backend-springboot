package com.oreum.posts.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostsDTO {
    private int postId;             // 게시글 고유 ID
    private int userId;             // 작성자 ID
    private int boardId;            // 소속 커뮤니티 ID
    private String type;            // 게시글 유형 (general, curation, meeting)
    private String title;           // 제목
    private String content;         // 본문 내용
    private int likeCount;          // 좋아요 수
    private int commentCount;       // 댓글 수
    private boolean isDeleted;      // 삭제 여부
    private LocalDateTime createdAt;// 작성일
    private LocalDateTime updatedAt;// 수정일
}
