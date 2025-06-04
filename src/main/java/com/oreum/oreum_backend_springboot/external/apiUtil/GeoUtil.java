package com.oreum.oreum_backend_springboot.external.apiUtil;

public class GeoUtil {

    /**
     * 위경도를 기상청 격자(nx, ny) 좌표로 변환
     * 
     * @param lat 위도
     * @param lon 경도
     * @return GridCoordinate (nx, ny)
     */
    public static GridCoordinate convertLatLonToGrid(double lat, double lon) {
        final double RE = 6371.00877; // 지구 반지름(km)
        final double GRID = 5.0;      // 격자 간격(km)
        final double SLAT1 = 30.0;    // 투영 위도1
        final double SLAT2 = 60.0;    // 투영 위도2
        final double OLON = 126.0;    // 기준 경도
        final double OLAT = 38.0;     // 기준 위도
        final double XO = 43;         // 기준 X좌표
        final double YO = 136;        // 기준 Y좌표

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

    // 결과로 반환할 좌표 객체 (nx, ny)
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
            return "GridCoordinate{nx=" + nx + ", ny=" + ny + '}';
        }
    }
}
