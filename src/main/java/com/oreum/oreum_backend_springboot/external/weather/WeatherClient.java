package com.oreum.oreum_backend_springboot.external.weather;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WeatherClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${external.kma.key}")
    private String serviceKey;

    public List<WeatherDTO> fetchWeather(int nx, int ny, String baseDate, String baseTime) {
        String url = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

        // WebClient 인스턴스 생성
        WebClient webClient = webClientBuilder.baseUrl(url).build();

        // JSON 응답을 요청 (.accept(MediaType.APPLICATION_JSON) 추가된 부분)
        Mono<JsonNode> response = webClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("serviceKey", serviceKey)
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1000)
                .queryParam("dataType", "JSON")  // 이걸로 JSON 응답 지정 충분
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .build())
            .retrieve()  
            .bodyToMono(JsonNode.class);


        // 응답에서 item 배열 꺼내기
        JsonNode items = response.block()
            .path("response")
            .path("body")
            .path("items")
            .path("item");

        return items.isArray()
            ? StreamSupport.stream(items.spliterator(), false)
                .map(WeatherDTO::fromJsonNode)
                .collect(Collectors.toList())
            : List.of();
    }

    @Value("${external.kakao.rest.key}")
    private String kakaoRestKey;

    // public LocationDTO fetchCoordinatesFromKakao(String query) {
    //     WebClient kakaoClient = webClientBuilder
    //         .baseUrl("https://dapi.kakao.com")
    //         .defaultHeader("Authorization", "KakaoAK " + kakaoRestKey)
    //         .build();

    //     JsonNode res = kakaoClient.get()
    //         .uri(uriBuilder -> uriBuilder
    //             .path("/v2/local/search/keyword.json")
    //             .queryParam("query", query)
    //             .build())
    //         .retrieve()
    //         .bodyToMono(JsonNode.class)
    //         .block();

    //     JsonNode first = res.path("documents").get(0);
    //     double lon = first.path("x").asDouble();
    //     double lat = first.path("y").asDouble();

    //     return new LocationDTO(lat, lon);
    // }

    public LocationDTO fetchCoordinatesFromKakao(String query) {
        System.out.println("📌 Kakao REST Key: KakaoAK " + kakaoRestKey);
        System.out.println("📌 Query: " + query);

        WebClient kakaoClient = webClientBuilder
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader("Authorization", "KakaoAK " + kakaoRestKey)
            .build();

        try {
            JsonNode res = kakaoClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

            System.out.println("📌 Kakao API 응답: " + res.toPrettyString());

            JsonNode first = res.path("documents").get(0);
            double lon = first.path("x").asDouble();
            double lat = first.path("y").asDouble();

            return new LocationDTO(lat, lon);

        } catch (Exception e) {
            System.out.println("❌ Kakao API 요청 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }




}