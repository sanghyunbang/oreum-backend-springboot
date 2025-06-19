package com.oreum.map.weather.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreum.map.mountains.DTO.MountainDTO;
import com.oreum.map.mountains.Service.MountainService;
import com.oreum.map.weather.DTO.WeatherDTO;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
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

    //Redis 관련 부분
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Redis에 넣기 전 직렬화를 위해서



    /**
     * 서버 시작 시 테스트용으로 1개 산만 업데이트
     */
    // @PostConstruct // 처음 시작하면 그때 바로 실행
    public void runOnceAtStartup() {
        System.out.println("[실행] 서버 부팅 시 전체 산 날씨 초기 업데이트 실행");
        scheduledFullUpdate();
    }

    // 전량 일괄 업데이트용 (API 제한이 넉넉할 경우만 사용)
    // @Scheduled(cron = "0 30 8 * * *", zone = "Asia/Seoul")
    public void scheduledFullUpdate() {
        System.out.println("[스케줄] 전 산 날씨 예보 업데이트 시작");

        String baseTime = LocalTime.now().isBefore(LocalTime.of(8, 30)) ? "1700" : "0800";
        LocalDate baseDate = baseTime.equals("1700") ? LocalDate.now().minusDays(1) : LocalDate.now();
        String formattedDate = baseDate.format(dateFormatter);

        List<MountainDTO> mountains = mountainService.getAll();
        ValueOperations<String,String> ops = redisTemplate.opsForValue();

        int batchSize = 5; // 한 번에 처리할 산 개수
        Flux.fromIterable(mountains)
            .buffer(batchSize) // 묶어서 처리
            .concatMap(batch -> Flux.fromIterable(batch)
                .delayElements(Duration.ofMillis(500)) // 각 요청 간 500ms 딜레이
                .flatMap(mountain -> {
                    int mountainNum = mountain.getMountainNum();
                    return weatherService.fetchFromKMAReactive(formattedDate, baseTime, mountainNum)
                            .retryWhen(reactor.util.retry.Retry.backoff(2, Duration.ofSeconds(3))) // 3초 간격 최대 2회 재시도
                            .doOnNext(data -> {
                                System.out.printf("[%s] %s → %d건 수신%n", mountain.getName(), mountainNum, data.size());
                                try {
                                    String key = "weather:" + mountainNum;
                                    String json = objectMapper.writeValueAsString(data);
                                    ops.set(key, json, Duration.ofHours(12));
                                } catch (JsonProcessingException e) {
                                    System.err.printf("[X] Redis 저장 실패 (%d): %s%n", mountainNum, e.getMessage());
                                }
                            })
                            .doOnError(err -> System.err.printf("[X] API 실패 ([%s %d]): %s%n",
                                    mountain.getName(), mountainNum, err.getMessage()));
                }))
            .doOnComplete(() -> System.out.println("[완료] 병렬+딜레이 기반 날씨 예보 업데이트 종료"))
            .subscribe();
    }

}
