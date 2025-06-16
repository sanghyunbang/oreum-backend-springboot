package com.oreum.map.weather.Controller;

import com.oreum.map.mountains.DTO.MountainDTO;
import com.oreum.map.mountains.Service.MountainService;
import com.oreum.map.weather.DTO.WeatherDTO;
import com.oreum.map.weather.Service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/weather")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class WeatherController {

    private final WeatherService weatherService;
    private final MountainService mountainService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * [GET] /weather/summit?mountainNum=1
     * → 해당 산의 실시간 예보 요청 및 정리된 형태 반환
     */
    @GetMapping("/summit")
    public Map<String, Object> getFormattedSummitWeather(@RequestParam(name = "mountainNum") int mountainNum) {    
        // 산 이름 가져오기
        String mountainName = mountainService.findByNum(mountainNum)
                .map(MountainDTO::getName)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 mountainNum: " + mountainNum));

        // 시간 조건에 따라 baseTime/baseDate 결정
        LocalTime now = LocalTime.now();
        String baseTime = now.isBefore(LocalTime.of(8, 30)) ? "1700" : "0800";
        LocalDate baseDate = baseTime.equals("1700") ? LocalDate.now().minusDays(1) : LocalDate.now();
        String baseDateStr = baseDate.format(DATE_FORMATTER);

        System.out.printf("[요청] %s (%s %s)%n", mountainName, baseDateStr, baseTime);

        // 실시간 호출 및 가공된 결과 반환
        return weatherService.getFormattedForecast(mountainNum);

    }

    /**
     * 수동 업데이트 (일괄 요청용 - 더 이상 사용하지 않으면 제거 가능)
     */
    /*
    @GetMapping("/update/manual")
    public String triggerManualUpdate(@RequestParam String baseTime) {
        scheduler.runUpdate(LocalDate.now(), baseTime);
        return "[O] 업데이트 실행 완료 (" + baseTime + ")";
    }
    */
}
