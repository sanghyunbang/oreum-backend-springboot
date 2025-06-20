package com.oreum.auth.jwt;

import com.oreum.auth.dto.AppUserRecordDTO; // AppUserRecordDTO 임포트 추가
import com.oreum.auth.mapper.AppUserDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User; // org.springframework.security.core.userdetails.User 임포트
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class AppCustomSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Autowired
    private AppUserDao appUserMapper;

    public AppCustomSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // 1. 로그인된 사용자 정보 가져오기
        // 일반 로그인에서는 org.springframework.security.core.userdetails.User 객체를 반환합니다.
        // OAuth2 로그인에서는 CustomOAuth2User를 반환합니다.
        // AppCustomSuccessHandler는 일반 로그인에 사용되므로 User 타입을 가정합니다.
        // 혹시 다른 타입을 받을 경우를 대비하여 instanceof 검사를 추가하거나,
        // 현재는 직접 User 타입으로 캐스팅합니다.
        User userDetails = (User) authentication.getPrincipal(); // ★★★ 이 부분 수정 ★★★
        String username = userDetails.getUsername(); // User 객체에서 username (이메일) 가져오기

        // 2. userId, nickname 조회
        // DB에서 사용자 정보(nickname, userId)를 다시 조회합니다.
        // OAuth2 로그인에서는 이미 customUserDetails에 nickname이 있지만, 일반 로그인에서는 User 객체에 nickname이 없습니다.
        // AppUserDao를 사용하여 DB에서 조회하는 것이 안전합니다.
        AppUserRecordDTO userInfo = appUserMapper.findByEmail(username); // AppUserRecordDTO 사용
        int userId = userInfo.getUserId();
        String nickname = userInfo.getNickname(); // 조회된 DTO에서 닉네임 가져오기

        // 3. 권한(role) 추출
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        // 4. JWT 생성 (access: 30분, refresh: 14일)
        String accessToken = jwtUtil.createJwt(username, nickname, role, 1000L * 60 * 30); // 30분
        String refreshToken = jwtUtil.createJwt(username, nickname, role, 1000L * 60 * 60 * 24 * 14); // 14일

        // 5. 보안 쿠키로 JWT 저장 (HttpOnly = true)
        response.addCookie(createCookie("accessToken", accessToken, true));
        response.addCookie(createCookie("refreshToken", refreshToken, true));

        // 6. 디버깅 로그
        System.out.println("[AppCustomSuccessHandler: 일반 로그인 성공]");
        System.out.println("사용자: " + username + ", 닉네임: " + nickname + ", 역할: " + role);
        System.out.println("accessToken=" + accessToken);
        System.out.println("refreshToken=" + refreshToken);

        // 7. 리다이렉트: Flutter 앱의 메인 페이지 또는 로그인 성공 페이지로 리다이렉트
        // OAuth2 리다이렉트 URL과 다르게 설정해야 할 수 있습니다.
        // 일반적으로 API 요청 후에는 JSON 응답을 보내거나, 특정 성공 URL로 리다이렉트합니다.
        // Flutter 앱의 경우 백엔드에서 JSON 응답을 받는 것이 일반적이므로,
        // 여기서는 예시로 "로그인 성공" 메시지를 JSON으로 보내는 것으로 변경합니다.
        // 만약 리다이렉트가 필요하다면 Flutter 앱의 로그인 성공 후 라우팅 로직을 참고하여 설정해야 합니다.
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\": \"Login successful\", \"username\": \"" + username + "\", \"nickname\": \"" + nickname + "\", \"role\": \"" + role + "\"}");
        // response.sendRedirect("http://10.0.2.2:3000/main"); // 만약 리다이렉트가 필요하다면 이 주석을 해제하고 경로를 지정 (Flutter 앱이 웹인 경우)
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