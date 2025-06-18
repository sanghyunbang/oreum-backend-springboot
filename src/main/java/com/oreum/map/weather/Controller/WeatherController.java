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

        
        // Redis 키 형식
        String redisKey = "weather:" + mountainNum;        
        try {
            // redis에서 데이터 조회
            String json = weatherService.getFromRedis(redisKey);
            if (json == null) {
                return Map.of("error","해당 산의 예보 데이터가 Redis에 존재하지 않습니다. 관리자에게 문의하세요.");
            }

            // JSON -> List<WeatherDTO>
            List<WeatherDTO> weatherList = weatherService.deserializeWeatherList(json);

            // 필요한 가공 로직 적용
            Map<String, Object> formatted = weatherService.formatForecast(weatherList);
            System.out.printf("[Redis 사용] %s (%s)%n", mountainName, redisKey);
            return formatted;
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "Redis 또는 데이터 처리 중 오류 발생: " + e.getMessage());            
        }
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
