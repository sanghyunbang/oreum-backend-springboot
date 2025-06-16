package com.oreum.map.weather.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherDTO {
    private String baseDate;
    private String bastTime; // 오타 주의: 실제 응답에 따라 "baseTime"으로 수정 가능
    private String category;
    private String fcstBase;
    private String fcstTime;
    private String fcstValue;
    private int nx;
    private int ny;
    private String lat;
    private String lon;
    private String alt;
    private String stn_nm; // 산 이름 (mountainNum 대신 사용)
}
