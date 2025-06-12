package com.oreum.auth.jwt;

import com.oreum.auth.dto.CustomOAuth2User;
import com.oreum.auth.mapper.UserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Autowired
    private UserDao userMapper;

    public CustomSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. 로그인된 사용자 정보 가져오기
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUserName();

        // 2. userId, nickname 조회
        int userId = userMapper.selectUserIdByEmail(username);
        String nickname = userMapper.userNameByuserId(userId);

        // 3. 권한(role) 추출
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        // 4. JWT 생성 (30분, 14일)
        String accessToken = jwtUtil.createJwt(username, nickname, role, 1000L * 60 * 30 * 2000); // 30분*2000
        String refreshToken = jwtUtil.createJwt(username, nickname, role, 1000L * 60 * 60 * 24 * 14); // 14일

        // 5. 쿠키 생성 및 등록 (setHeader는 제거)
        response.addCookie(createCookie("accessToken", accessToken, false));
        response.addCookie(createCookie("refreshToken", refreshToken, true));

        // 6. 디버깅 로그
        System.out.println("[로그인 성공]");
        System.out.println("사용자: " + username + ", role: " + role);
        System.out.println("accessToken=" + accessToken);
        System.out.println("refreshToken=" + refreshToken);

        // 7. 프론트엔드 리다이렉트 URL
        String redirectUrl = "http://localhost:3000/oauth2/redirect"
                + "?token=" + accessToken
                + "&email=" + username
                + "&nickname=" + URLEncoder.encode(nickname, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }

    private Cookie createCookie(String key, String value, boolean httpOnly) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(false); // 운영 배포 시 true
        // cookie.setDomain("localhost"); // 생략: localhost일 때는 설정하지 않는 것이 안전
        return cookie;
    }
}
