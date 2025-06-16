package com.oreum.map.weather.Service;

import com.oreum.map.mountains.DTO.MountainDTO;
import com.oreum.map.mountains.Service.MountainService;
import com.oreum.map.weather.DTO.WeatherDTO;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeatherService {

    // 외부에서 주입된 RestTemplate 사용 (new 제거)
    private final RestTemplate restTemplate;

    private final MountainService mountainService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

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
        String serviceKey = "-lGWcbEcRbSRlnGxHKW08w"; // 인증키
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
}


// package com.oreum.map.weather.Service;

// import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// import com.oreum.map.weather.DTO.WeatherDTO;

// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import java.util.*;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.stream.Collectors;

// @Service
// public class WeatherService {

//     // 산 이름(stn_nm) 기준 캐시 (mountainNum → stn_nm 매핑 필요)
//     private final Map<String, List<WeatherDTO>> weatherCache = new ConcurrentHashMap<>();

//     // 외부에서 받아온 날씨 데이터를 캐시에 저장 (stn_nm 기준 그룹화)
//     public void updateWeather(List<WeatherDTO> newData) {
//         Map<String, List<WeatherDTO>> grouped = newData.stream()
//                 .collect(Collectors.groupingBy(WeatherDTO::getStn_nm));
//         weatherCache.putAll(grouped);
//     }

//     // 특정 산(이름)의 날씨 정보
//     public List<WeatherDTO> getWeatherForMountain(String mountainName) {
//         return weatherCache.getOrDefault(mountainName, Collections.emptyList());
//     }

//     // 날짜별 0900, 1500, 2100 시간대 TMP, PCP, SKY, 일출/일몰 추출
//     public Map<String, Object> getFormattedForecast(String mountainName) {
//         List<WeatherDTO> list = getWeatherForMountain(mountainName);

//         List<String> targetTimes = List.of("0900", "1500", "2100");
//         List<String> targetCategories = List.of("TMP", "PCP", "SKY", "SRE", "SSE");

//         Map<String, Map<String, String>> result = new TreeMap<>();

//         for (WeatherDTO dto : list) {
//             if (!targetTimes.contains(dto.getFcstTime()) && !targetCategories.contains(dto.getCategory()))
//                 continue;

//             String date = dto.getFcstBase();
//             result.putIfAbsent(date, new HashMap<>());

//             String time = dto.getFcstTime();
//             String key = switch (dto.getCategory()) {
//                 case "TMP" -> time + "_temp";
//                 case "PCP" -> time + "_pcp";
//                 case "SKY" -> time + "_sky";
//                 case "SRE" -> "sunrise";
//                 case "SSE" -> "sunset";
//                 default -> null;
//             };

//             if (key != null) {
//                 result.get(date).put(key, dto.getFcstValue());
//             }
//         }

//         return Map.of("forecast", result);
//     }

//     // API 호출 (KMA 산악예보 API → JSON 배열 응답)
//     public List<WeatherDTO> fetchFromKMA(String baseDate, String baseTime, int mountainNum) {
//         String serviceKey = "-lGWcbEcRbSRlnGxHKW08w"; // 발급받은 인증키
//         String url = String.format(
//             "https://apihub.kma.go.kr/api/typ08/getMountainWeather?mountainNum=%d&base_date=%s&base_time=%s&authKey=%s",
//             mountainNum, baseDate, baseTime, serviceKey
//         );

//         try {
//             RestTemplate restTemplate = new RestTemplate();
//             ResponseEntity<WeatherDTO[]> response = restTemplate.getForEntity(url, WeatherDTO[].class);
//             WeatherDTO[] body = response.getBody();

//             if (body != null) {
//                 return List.of(body);
//             } else {
//                 return Collections.emptyList();
//             }

//         } catch (Exception e) {
//             System.err.println("[!] 산악예보 API 호출 실패: " + e.getMessage());
//             return Collections.emptyList();
//         }
//     }
// }
