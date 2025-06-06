package com.oreum.external.kma.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.JsonNode;


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

    // JSON에서 DTO로 변환하는 정적 메서드
    public static WeatherDTO fromJsonNode(JsonNode node) {
        WeatherDTO dto = new WeatherDTO();
        dto.setBaseDate(node.path("baseDate").asText());
        dto.setBaseTime(node.path("baseTime").asText());
        dto.setCategory(node.path("category").asText());
        dto.setFcstDate(node.path("fcstDate").asText());
        dto.setFcstTime(node.path("fcstTime").asText());
        dto.setFcstValue(node.path("fcstValue").asText());
        dto.setNx(node.path("nx").asInt());
        dto.setNy(node.path("ny").asInt());
        return dto;
    }
}

