package com.oreum.community.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class communityDTO {
    private int boardId;
    private String name;
    private String title;
    private String description;
    private int creatorId;
    private String creatorNickname;
    private String thumbnailUrl;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
