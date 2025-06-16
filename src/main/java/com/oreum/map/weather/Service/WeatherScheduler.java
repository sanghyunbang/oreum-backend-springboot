package com.oreum.map.weather.Service;

import com.oreum.map.mountains.DTO.MountainDTO;
import com.oreum.map.mountains.Service.MountainService;
import com.oreum.map.weather.DTO.WeatherDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WeatherScheduler {

    private final WeatherService weatherService;
    private final MountainService mountainService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 서버 시작 시 테스트용으로 1개 산만 업데이트
     */
    // @PostConstruct → 실시간 호출로 바뀌었으므로 주석 처리 가능
    public void runOnceAtStartup() {
        System.out.println("[실행] 서버 부팅 시 특정 산 날씨 테스트");

        int sampleMountainNum = 1; // 예: 설악산
        LocalDate now = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        String baseTime = nowTime.isBefore(LocalTime.of(8, 30)) ? "1700" : "0800";
        LocalDate baseDate = baseTime.equals("1700") ? now.minusDays(1) : now;

        List<WeatherDTO> result = weatherService.fetchFromKMA(baseDate.format(dateFormatter), baseTime, sampleMountainNum);
        System.out.printf("[테스트] %s %s - %d건 데이터 수신 완료%n", baseDate, baseTime, result.size());
    }

    // 전량 일괄 업데이트용 (API 제한이 넉넉할 경우만 사용)
    // @Scheduled(cron = "0 30 8 * * *", zone = "Asia/Seoul")
    public void scheduledFullUpdate() {
        System.out.println("[스케줄] 전 산 날씨 예보 업데이트 시작");
        String baseTime = LocalTime.now().isBefore(LocalTime.of(8, 30)) ? "1700" : "0800";
        LocalDate baseDate = baseTime.equals("1700") ? LocalDate.now().minusDays(1) : LocalDate.now();
        String formattedDate = baseDate.format(dateFormatter);

        int success = 0, fail = 0;

        for (MountainDTO mountain : mountainService.getAll()) {
            int mountainNum = mountain.getMountainNum();
            try {
                List<WeatherDTO> data = weatherService.fetchFromKMA(formattedDate, baseTime, mountainNum);
                System.out.printf("⛰️ [%s] %s → %d건 수신%n", mountain.getName(), mountainNum, data.size());
                success++;

                Thread.sleep(800); // 속도 제한 (API 안정)

            } catch (Exception e) {
                System.err.printf("[X] [%s] %s → 실패 (%s)%n", mountain.getName(), mountainNum, e.getMessage());
                fail++;
            }
        }

        System.out.printf("[완료] 성공: %d / 실패: %d%n", success, fail);
    }
}
