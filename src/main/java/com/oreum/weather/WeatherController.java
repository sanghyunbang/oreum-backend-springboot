package com.oreum.weather;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oreum.external.kma.dto.MountainNameDTO;
import com.oreum.external.kma.dto.WeatherDTO;
import com.oreum.external.kma.service.WeatherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @PostMapping("/summit")
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    public List<WeatherDTO> getSummitWeather(@RequestBody MountainNameDTO request) {
    String mountainName = request.getMountainName();
    return weatherService.getWeatherByMountainName(mountainName);
}

}

