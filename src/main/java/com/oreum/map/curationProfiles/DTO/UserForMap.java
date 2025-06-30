package com.oreum.map.curationProfiles.DTO;

import lombok.Data;

@Data
public class UserForMap {

    private int id;
    private String nickname;
    private double lat;
    private double lon;
    private int postId;
    private String profileUrl; // "" 경우는 따로 처리

}
