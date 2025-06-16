// GeoUtil.java
package com.oreum.external.apiUtil;

import com.oreum.external.kma.dto.GridCoordinateWithLatLon;
import com.oreum.external.kma.dto.WeatherDTO;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeoUtil {

    /**
     * 위경도를 기상청 격자(nx, ny) 좌표로 변환
     */
    public static GridCoordinate convertLatLonToGrid(double lat, double lon) {
        final double RE = 6371.00877;
        final double GRID = 5.0;
        final double SLAT1 = 30.0;
        final double SLAT2 = 60.0;
        final double OLON = 126.0;
        final double OLAT = 38.0;
        final double XO = 43;
        final double YO = 136;

        final double DEGRAD = Math.PI / 180.0;
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);

        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;

        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);

        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        int nx = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
        int ny = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

        return new GridCoordinate(nx, ny);
    }

    /**
     * gridList에서 가장 가까운 격자 좌표 찾기
     */
    public static GridCoordinateWithLatLon findNearestGrid(double targetLat, double targetLon, List<GridCoordinateWithLatLon> gridList) {
        return gridList.stream()
                .min(Comparator.comparingDouble(grid -> haversine(grid.getLat(), grid.getLon(), targetLat, targetLon)))
                .orElseThrow(() -> new RuntimeException("No grid data available"));
    }

    /**
     * 정확한 nx/ny가 있으면 사용, 없으면 fallback으로 가장 가까운 좌표 사용
     */
    public static GridCoordinateWithLatLon resolveGridCoordinate(double lat, double lon, List<GridCoordinateWithLatLon> gridList) {
        GridCoordinate grid = convertLatLonToGrid(lat, lon);
        return gridList.stream()
                .filter(g -> g.getNx() == grid.getNx() && g.getNy() == grid.getNy())
                .findFirst()
                .orElseGet(() -> findNearestGrid(lat, lon, gridList));
    }

    /**
     * 두 위경도 좌표 간의 haversine 거리 계산 (단위: km)
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static Map<String, Map<String, String>> summarizeWeather(List<WeatherDTO> weatherList) {
        Map<String, Map<String, String>> summaryMap = new LinkedHashMap<>();

        for (WeatherDTO dto : weatherList) {
            String time = dto.getFcstDate() + " " + dto.getFcstTime();
            summaryMap.putIfAbsent(time, new HashMap<>());
            summaryMap.get(time).put(dto.getCategory(), dto.getFcstValue());
        }

        return summaryMap;
    }

    public static void printSummarizedWeather(Map<String, Map<String, String>> summarized) {
        for (Map.Entry<String, Map<String, String>> entry : summarized.entrySet()) {
            String time = entry.getKey();
            Map<String, String> values = entry.getValue();
            System.out.println("[" + time + "]");
            System.out.println("- SKY (하늘 상태): " + values.getOrDefault("SKY", "N/A"));
            System.out.println("- TMP (기온): " + values.getOrDefault("TMP", "N/A") + " °C");
            System.out.println("- REH (습도): " + values.getOrDefault("REH", "N/A") + " %");
            System.out.println("- PCP (강수량): " + values.getOrDefault("PCP", "없음"));
            System.out.println();
        }
    }

    public static class GridCoordinate {
        private final int nx;
        private final int ny;

        public GridCoordinate(int nx, int ny) {
            this.nx = nx;
            this.ny = ny;
        }

        public int getNx() {
            return nx;
        }

        public int getNy() {
            return ny;
        }

        @Override
        public String toString() {
            return "GridCoordinate{" +
                    "nx=" + nx +
                    ", ny=" + ny +
                    '}';
        }
    }
} 