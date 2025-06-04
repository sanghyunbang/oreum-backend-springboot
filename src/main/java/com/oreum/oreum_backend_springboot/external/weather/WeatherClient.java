package com.oreum.oreum_backend_springboot.external.weather;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;


@Component
@RequiredArgsConstructor
public class WeatherClient {

    private final RestTemplate restTemplate;

    @Value("${external.kma.key}") // application.properties에서 주입
    private String serviceKey;

    public List<WeatherDTO> fetchWeather(int nx, int ny, String baseDate, String baseTime) {
        String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst" +
            "?serviceKey=" + serviceKey +
            "&pageNo=1&numOfRows=1000&dataType=JSON" +
            "&base_date=" + baseDate +
            "&base_time=" + baseTime +
            "&nx=" + nx +
            "&ny=" + ny;

        ResponseEntity<JsonNode> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            JsonNode.class
        );

        JsonNode items = response.getBody()
                                  .path("response")
                                  .path("body")
                                  .path("items")
                                  .path("item");

        ObjectMapper mapper = new ObjectMapper();
        try{
            return Arrays.asList(mapper.treeToValue(items, WeatherDTO[].class));
        } catch (JsonProcessingException e){
            throw new RuntimeException("기상청 응답 파싱 실패", e);
        }
    }
}
