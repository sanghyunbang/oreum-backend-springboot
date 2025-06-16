package com.oreum.map.mountains.DTO;

import lombok.Data;

@Data
public class MountainDTO {
    private int mountainNum;
    private String name;
    private double lat;
    private double lon;
    private int alt;
}
