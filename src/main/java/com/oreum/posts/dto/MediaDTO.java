package com.oreum.posts.dto;

import lombok.Data;

@Data
public class MediaDTO {
    private int mediaId;
    private int postId;
    private String mediaType; //"image" or "video"
    private String mediaUrl;
    private String createdAt;
}