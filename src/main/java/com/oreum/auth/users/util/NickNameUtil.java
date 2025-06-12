package com.oreum.auth.users.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NickNameUtil {

        private static final Random random = new Random();

    private static final List<String> ADJECTIVES = new ArrayList<>(List.of(
        "행복한", "신비한", "용감한", "느긋한", "조용한", "열정적인",
        "차가운", "뜨거운", "사려깊은", "재치있는", "겸손한", "근엄한"
        // 여기 계속 추가 가능 (또는 외부에서 불러오기)
    ));

    private static final List<String> NOUNS = new ArrayList<>(List.of(
        "호랑이", "고양이", "다람쥐", "돌고래", "부엉이", "여우",
        "거북이", "독수리", "늑대", "기린", "사슴", "판다"
        // 여기도 확장 가능
    ));

    public static String generateRandomNickname() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(random.nextInt(NOUNS.size()));
        int number = 1000 + random.nextInt(9000); // 1000~9999 → 9000개

        return adjective + noun + number;
    }

    // public static int getPossibleCombinations() {
    //     return ADJECTIVES.size() * NOUNS.size() * 9000;
    // }
    
}
