package com.oreum.external.kma.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.oreum.external.kma.dto.GridCoordinateWithLatLon;

import jakarta.annotation.PostConstruct;

@Component
public class GridLoader {

    private List<GridCoordinateWithLatLon> gridList = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        System.out.println("🌀 GridLoader 초기화 시작...");

        // 정식 CSV 파일 로드
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/weather_grid.csv");

        // 테스트용 목 CSV 데이터 사용 시 아래 주석 해제
        // String mockCsv = "nx,ny,latitude,longitude\n60,127,37.5665,126.9780\n61,128,37.5700,126.9900";
        // InputStream is = new ByteArrayInputStream(mockCsv.getBytes(StandardCharsets.UTF_8));

        if (is == null) {
            throw new FileNotFoundException("[X] CSV 파일을 찾을 수 없습니다: data/weather_grid.csv");
        }

        System.out.println("[O] CSV 파일 로딩 성공");

        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        String line = br.readLine(); // 첫 줄 헤더는 무시
        int lineNumber = 1;

        while ((line = br.readLine()) != null) {
            lineNumber++;
            try {
                String[] parts = line.split(",");

                int nx = Integer.parseInt(parts[5].trim());
                int ny = Integer.parseInt(parts[6].trim());
                double lon = Double.parseDouble(parts[13].trim());
                double lat = Double.parseDouble(parts[14].trim());

                GridCoordinateWithLatLon coord = new GridCoordinateWithLatLon(lat, lon, nx, ny);
                gridList.add(coord);

                // 디버깅 로그
                System.out.printf("[V] [%d] lat=%.4f, lon=%.4f, nx=%d, ny=%d%n", lineNumber, lat, lon, nx, ny);
            } catch (Exception e) {
                System.err.printf("[!] [Line %d] 파싱 실패: %s%n", lineNumber, e.getMessage());
            }
        }

        br.close();
        System.out.printf("[*] 총 %d개의 그리드 좌표가 로드되었습니다.%n", gridList.size());
    }

    public List<GridCoordinateWithLatLon> getGridList() {
        return gridList;
    }
}
