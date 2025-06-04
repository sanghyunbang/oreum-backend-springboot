package com.oreum.oreum_backend_springboot.external.weather;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.oreum.oreum_backend_springboot.external.apiUtil.GeoUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {
    
    private final WeatherClient weatherClient;

    public List<WeatherDTO> getweatherForLocation(String mountainName) {

        // 산 이름 -> 위경도 변환(kakao api) 쓸 예정
        double lat = 37.6; // 예시 값
        double lon = 126.9;

        // 위경도 -> nx, ny 격자를 변환
        var grid = GeoUtil.convertLatLonToGrid(lat, lon);

        // 날짜/시간 지정
        // String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseDate = "20250604";

        String baseTime = "2300";

        // 데이터 요청
        List<WeatherDTO> result = weatherClient.fetchWeather(grid.getNx(), grid.getNy(), baseDate, baseTime);

        // ✅ 로그 출력
        System.out.println("✅ 날씨 데이터 개수: " + result.size());
        result.forEach(dto -> System.out.println(dto));

        return result;
    }  
}
