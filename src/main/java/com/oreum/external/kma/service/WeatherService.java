// // WeatherService.java
// package com.oreum.external.kma.service;

// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.stream.Collectors;

// import org.springframework.stereotype.Service;

// import com.oreum.external.kma.client.WeatherClient;
// import com.oreum.external.kma.dto.GridCoordinateWithLatLon;
// import com.oreum.external.kma.dto.WeatherDTO;
// import com.oreum.external.apiUtil.GeoUtil;
// import com.oreum.external.apiUtil.WeatherTimeUtil;

// import lombok.RequiredArgsConstructor;


// public class WeatherService {
    
// }

// @Service
// @RequiredArgsConstructor
// public class WeatherService {

//     private final WeatherClient weatherClient;
//     private final List<GridCoordinateWithLatLon> gridList;

//     public Map<String, List<WeatherDTO>> getWeatherByMountainName(String mountainName) {
//         // 1. 카카오 API로부터 위도 경도 가져오기
//         var coord = weatherClient.fetchCoordinatesFromKakao(mountainName);

//         // 2. 공식 변환 -> 정확한 격자가 없으면 가장 가까운 grid fallback
//         var resolved = GeoUtil.resolveGridCoordinate(coord.getLat(), coord.getLon(), gridList);

//         // 3. 예보 기준 날짜/시간 계산
//         var timeInfo = WeatherTimeUtil.getLatestBaseDateTime();
//         String baseDate = timeInfo.get("baseDate");
//         String baseTime = timeInfo.get("baseTime");

//         // 4. 날씨 데이터 요청
//         List<WeatherDTO> all = weatherClient.fetchWeather(resolved.getNx(), resolved.getNy(), baseDate, baseTime);

//         // 5. 주요 항목만 필터링
//         Set<String> wanted = Set.of("SKY", "PTY", "TMP", "REH", "PCP");
//         List<WeatherDTO> filtered = all.stream()
//                 .filter(dto -> wanted.contains(dto.getCategory()))
//                 .collect(Collectors.toList());

//         // 6. 시간대별로 그룹핑
//         Map<String, List<WeatherDTO>> groupedByTime = filtered.stream()
//                 .collect(Collectors.groupingBy(dto -> dto.getFcstDate() + " " + dto.getFcstTime()));

//         // 로그 출력
//         groupedByTime.forEach((k, v) -> {
//             System.out.println("[" + k + "]");
//             v.forEach(d -> System.out.println("- " + d.getCategory() + ": " + d.getFcstValue()));
//         });

//         return groupedByTime;
//     }
// }
