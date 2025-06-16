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

        // 4. JWT 생성 (access: 30분, refresh: 14일)
        String accessToken = jwtUtil.createJwt(username, nickname, role, 1000L * 60 * 30 * 1000); // 30분 * 1000
        String refreshToken = jwtUtil.createJwt(username, nickname, role, 1000L * 60 * 60 * 24 * 14); // 14일

        // 5. 보안 쿠키로 JWT 저장 (HttpOnly = true)
        response.addCookie(createCookie("accessToken", accessToken, true)); // ⬅ HttpOnly로 수정
        response.addCookie(createCookie("refreshToken", refreshToken, true));

        // 6. 디버깅 로그
        System.out.println("[로그인 성공]");
        System.out.println("사용자: " + username + ", role: " + role);
        System.out.println("accessToken=" + accessToken);
        System.out.println("refreshToken=" + refreshToken);

        // 7. 리다이렉트: 보안 강화를 위해 토큰은 URL에 포함시키지 않음
        String redirectUrl = "http://localhost:3000/oauth2/redirect"
                + "?email=" + username
                + "&nickname=" + URLEncoder.encode(nickname, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }

    // 보안 설정이 적용된 쿠키 생성 메서드
    private Cookie createCookie(String key, String value, boolean httpOnly) {
        Cookie cookie = new Cookie(key, value);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        cookie.setHttpOnly(httpOnly);       // JS 접근 차단
        cookie.setSecure(false);            // 배포 시에는 true로 (https)
        return cookie;
    }
}
