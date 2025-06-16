package com.oreum.external.apiUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class WeatherTimeUtil {

    private static final String[] BASE_TIME = {"2300", "2000", "1700", "1400", "1100", "0800", "0500", "0200"};

    public static String getBaseDate(LocalDateTime now) {
        String baseTime = getBaseTime(now);
        if (baseTime.equals("2300") && now.getHour()<2){
            return now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    public static String getBaseTime(LocalDateTime now) {
        int hour = now.getHour();
        // int minute = now.getMinute();

        for(String time : BASE_TIME) {
            int baseHour = Integer.parseInt(time.substring(0,2));

            if(hour > baseHour) {
                return time;
            }
        }
        return "2300"; // 디폴트 값은 전날 23시
    }

    public static Map<String, String> getLatestBaseDateTime() {
    LocalDateTime now = LocalDateTime.now();
    String baseDate = getBaseDate(now);
    String baseTime = getBaseTime(now);

    Map<String, String> result = new HashMap<>();
    result.put("baseDate", baseDate);
    result.put("baseTime", baseTime);
    return result;
    }
    
}
