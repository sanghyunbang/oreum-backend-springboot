package com.oreum.external.kma.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.oreum.external.kma.client.WeatherClient;
import com.oreum.external.kma.dto.WeatherDTO;
import com.oreum.external.apiUtil.GeoUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WeatherService {
    
    private final WeatherClient weatherClient;

    public List<WeatherDTO> getWeatherByMountainName(String mountainName) {

        // 카카오 api 호출로 위경도 얻기
        var coord = weatherClient.fetchCoordinatesFromKakao(mountainName);

        // 위경도 -> nx, ny 격자를 변환
        var grid = GeoUtil.convertLatLonToGrid(coord.getLat(),coord.getLon());

        // 날짜/시간 지정
        // String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseDate = "20250604";

        String baseTime = "2300";

        // 데이터 요청
        List<WeatherDTO> result = weatherClient.fetchWeather(grid.getNx(), grid.getNy(), baseDate, baseTime);

        // 로그 출력
        System.out.println("날씨 데이터 개수: " + result.size());
        result.forEach(dto -> System.out.println(dto));

        return result;
    }  
}