package com.oreum.oreum_backend_springboot.external.weather;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/sample")
    public List<WeatherDTO> getSampleWeather() {
        return weatherService.getweatherForLocation("북한산"); // 예시 산 이름
    }
    
}
