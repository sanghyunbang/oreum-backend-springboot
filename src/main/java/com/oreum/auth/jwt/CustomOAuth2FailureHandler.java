package com.oreum.auth.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        // ✅ 세션 무효화 (재시도 시 stale한 인증 정보 방지)
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        // ✅ 로그: 이유 출력
        System.out.println("소셜 로그인 실패: " + exception.getMessage());

        // ✅ 프론트로 리다이렉트 (React에서 에러 메시지 표시 가능)
        response.sendRedirect("/login?error=oauth2");
    }
}


