package com.oreum.oreum_backend_springboot.external.weather;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LocationDTO {
    private double lat;
    private double lon;
}
