package com.oreum.oreum_backend_springboot.external.weather;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 외부 API 응답을 담을 곳
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDTO {
    private String baseDate;
    private String baseTime;
    private String category;
    private String fcstDate;
    private String fcstTime;
    private String fcstValue;
    private int nx;
    private int ny;
}
