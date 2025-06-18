package com.oreum.map.weather.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreum.map.mountains.DTO.MountainDTO;
import com.oreum.map.mountains.Service.MountainService;
import com.oreum.map.weather.DTO.WeatherDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeatherService {

    // 키값
    @Value("${external.kma.key}")
    private String serviceKey;

    // 외부에서 주입된 RestTemplate 사용 (new 제거)
    private final RestTemplate restTemplate;

    private final MountainService mountainService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    // WebClient 주입
    private final WebClient webClient;

    private final StringRedisTemplate redisTemplate; // Redis 연동 필드
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 직렬화 / 역직렬화

    /**
     * 클라이언트 요청 시, 산 번호 기반 실시간 날씨 호출 및 포맷
     */
    public Map<String, Object> getFormattedForecast(int mountainNum) {
        MountainDTO mountain = mountainService.findByNum(mountainNum)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 mountainNum: " + mountainNum));

        LocalDate now = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        String baseTime = nowTime.isBefore(LocalTime.of(8, 30)) ? "1700" : "0800";
        LocalDate baseDate = baseTime.equals("1700") ? now.minusDays(1) : now;

        List<WeatherDTO> rawData = fetchFromKMA(baseDate.format(dateFormatter), baseTime, mountainNum);

        Map<String, Map<String, String>> result = new TreeMap<>();
        List<String> targetTimes = List.of("0900", "1500", "2100");
        List<String> targetCategories = List.of("TMP", "PCP", "SKY", "SRE", "SSE");



        for (WeatherDTO dto : rawData) {
            if (!targetCategories.contains(dto.getCategory())) continue;
            if (!dto.getCategory().equals("SRE") && !dto.getCategory().equals("SSE")
                && !targetTimes.contains(dto.getFcstTime())) continue;

            String date = dto.getFcstBase();
            result.putIfAbsent(date, new HashMap<>());

            String time = dto.getFcstTime();
            String key = switch (dto.getCategory()) {
                case "TMP" -> time + "_temp";
                case "PCP" -> time + "_pcp";
                case "SKY" -> time + "_sky";
                case "SRE" -> "sunrise";
                case "SSE" -> "sunset";
                default -> null;
            };

            if (key != null) {
                result.get(date).put(key, dto.getFcstValue());
            }
        }

        return Map.of(
                "mountain", mountain.getName(),
                "lat", mountain.getLat(),
                "lon", mountain.getLon(),
                "alt", mountain.getAlt(),
                "baseDate", baseDate.format(dateFormatter),
                "baseTime", baseTime,
                "forecast", result
        );
    }

    /**
     * KMA 산악예보 API 호출
     */
    public List<WeatherDTO> fetchFromKMA(String baseDate, String baseTime, int mountainNum) {
        String url = String.format(
                "https://apihub.kma.go.kr/api/typ08/getMountainWeather?mountainNum=%d&base_date=%s&base_time=%s&authKey=%s",
                mountainNum, baseDate, baseTime, serviceKey
        );

        try {
            // RestTemplate는 주입된 인스턴스 사용
            ResponseEntity<WeatherDTO[]> response = restTemplate.getForEntity(url, WeatherDTO[].class);
            WeatherDTO[] body = response.getBody();
            return body != null ? List.of(body) : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("[!] 산악예보 API 호출 실패: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /*
     * WebClient를 사용한 비동기 호출(Mono<List<WeatherDTO>> 반환)
     */

     public Mono<List<WeatherDTO>> fetchFromKMAReactive(String baseDate, String baseTime, int mountainNum) {
        
        String url = String.format(
                "/api/typ08/getMountainWeather?mountainNum=%d&base_date=%s&base_time=%s&authKey=%s",
                mountainNum, baseDate, baseTime, serviceKey
        );

        return webClient.get() // [WebClient]를 통해서 GET 요청
                .uri(url) // 요청 보내는 곳 주소
                .retrieve() // 실제 HTTP 요청을 수행하고 응답 객체 추출 준비(??)
                .bodyToMono(WeatherDTO[].class) // 앞에 응답 본문을 WeatherDTO[] 배열로 비동기 변환
                .map(List::of) // map(responseArray -> List.of(ResponseArray))랑 같은 의미 '::'이거는 메서드 참조 문법
                .onErrorResume(e -> {
                    System.err.printf("[X] API 실패 (%d): %s%n", mountainNum, e.getMessage());
                    return Mono.just(List.of());
                });
     }

     // Redis에서 JSON 문자열 가져오기
     public String getFromRedis(String key) {
        return redisTemplate.opsForValue().get(key);
     }

     // JSON 문자열 -> List<WeatherDTO>
     public List<WeatherDTO> deserializeWeatherList(String json) throws JsonProcessingException {
        return objectMapper.readValue(json,
            objectMapper.getTypeFactory().constructCollectionType(List.class, WeatherDTO.class));
     }

     // 포맷 정리 -> 이전 방식에 맞춰서
    public Map<String, Object> formatForecast(List<WeatherDTO> weatherList) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 날짜별로 데이터 그룹화
        Map<String, Map<String, String>> forecastByDate = new TreeMap<>();

        for (WeatherDTO dto : weatherList) {
            String date = dto.getFcstBase();   // 예: "20250619"
            String time = dto.getFcstTime();   // 예: "0900"
            String category = dto.getCategory(); // 예: SKY, T1H, PCP
            String value = dto.getFcstValue(); // 예: "1.0"

            // 시간 필터링 (우리는 0900, 1500, 2100만 사용)
            if (!List.of("0900", "1500", "2100").contains(time)) continue;

            // 날짜별 forecast map 생성
            forecastByDate.putIfAbsent(date, new LinkedHashMap<>());
            Map<String, String> dailyForecast = forecastByDate.get(date);

            // 예보 항목 저장: 0900_temp, 1500_sky 등
            switch (category) {
                case "TMP":  // 기온
                    dailyForecast.put(time + "_temp", value);
                    break;
                case "SKY":  // 하늘 상태
                    dailyForecast.put(time + "_sky", value);
                    break;
                case "PCP":  // 강수량
                    dailyForecast.put(time + "_pcp", value);
                    break;
                case "SUNRISE":  // 선택적으로 사용
                    dailyForecast.put("sunrise", value);
                    break;
                case "SUNSET":
                    dailyForecast.put("sunset", value);
                    break;
            }
        }

        // 산 이름 넣기 (날씨 리스트에 포함된 첫 데이터 기준)
        String mountainName = weatherList.isEmpty() ? "?" : weatherList.get(0).getStn_nm();

        result.put("mountain", mountainName);
        result.put("forecast", forecastByDate);

        return result;
    }



}