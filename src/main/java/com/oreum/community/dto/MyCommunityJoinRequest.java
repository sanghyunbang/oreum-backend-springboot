package com.oreum.community.dto;

import lombok.Data;

@Data
public class MyCommunityJoinRequest {
    private int userId;
    private String communityTitle;
}
