package com.oreum.posts.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class BookmarkDTO {

	private int bookmarkId;       // 북마크 고유 ID
    private int userId;           // 사용자 ID
    private int postId;           // 게시글 ID
    private LocalDateTime createdAt; // 생성 시각
}
