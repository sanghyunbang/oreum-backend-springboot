package com.oreum.auth.jwt;

import com.oreum.auth.dto.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
public class CustomSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    public CustomSuccessHandler(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
        String username = customUserDetails.getUserName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        String role = iterator.hasNext() ? iterator.next().getAuthority() : "ROLE_USER";

        // JWT 생성
        String accessToken = jwtUtil.createJwt(username, role, 1000 * 60 * 30L); // 30분
        String refreshToken = jwtUtil.createJwt(username, role, 1000L * 60 * 60 * 24 * 14); // 14일

        // 쿠키로도 저장 (옵션)
        response.addCookie(createCookie("accessToken", accessToken, false));
        response.addCookie(createCookie("refreshToken", refreshToken, true));

        System.out.println("🎉 로그인 성공! 사용자: " + username + ", role: " + role);
        System.out.println("📦 accessToken=" + accessToken);
        System.out.println("📦 refreshToken=" + refreshToken);

        // 프론트에 전달할 redirect URI 구성
        String redirectUrl = "http://localhost:3000/oauth2/redirect"
                + "?token=" + accessToken
                + "&nickname=" + username;

        // 프론트로 리다이렉트
        response.sendRedirect(redirectUrl);
    }

    private Cookie createCookie(String key, String value, boolean httpOnly) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 24 * 14); // 14일
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(false); // ⚠️ 운영환경에서는 true
        return cookie;
    }
}
