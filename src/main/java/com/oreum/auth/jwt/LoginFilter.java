package com.oreum.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // BadCredentialsException 임포트
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = null;
        String password = null;

        try {
            // 요청 본문(request body)에서 JSON 데이터를 읽어 Map으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> loginRequest = objectMapper.readValue(request.getInputStream(), Map.class);

            username = loginRequest.get("username");
            password = loginRequest.get("password");

            // 추출된 값이 null이거나 비어있는지 확인
            if (username == null || username.trim().isEmpty()) {
                System.out.println("LoginFilter: Username cannot be null or empty.");
                throw new BadCredentialsException("Username cannot be null or empty.");
            }
            if (password == null || password.trim().isEmpty()) {
                System.out.println("LoginFilter: Password cannot be null or empty.");
                throw new BadCredentialsException("Password cannot be null or empty.");
            }

            System.out.println("LoginFilter: Attempting authentication for username: " + username);
            // 개발 환경에서만 비밀번호 로깅 (운영 환경에서는 절대 금지)
            System.out.println("LoginFilter: Password received: [REDACTED]"); 

            // UsernamePasswordAuthenticationToken 생성하여 AuthenticationManager에 전달
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password); // authorities는 기본적으로 null
            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            System.out.println("LoginFilter: Failed to parse login request body - " + e.getMessage());
            throw new BadCredentialsException("Failed to parse login request body", e);
        } catch (AuthenticationException e) {
            // Spring Security가 던지는 AuthenticationException (예: BadCredentialsException, UsernameNotFoundException)
            System.out.println("LoginFilter: Authentication failed for " + username + " - " + e.getMessage());
            throw e; // AuthenticationManager에서 발생한 예외를 그대로 다시 던집니다.
        } catch (Exception e) { // 예상치 못한 다른 예외 처리
            System.out.println("LoginFilter: An unexpected error occurred - " + e.getMessage());
            throw new BadCredentialsException("An unexpected error occurred during authentication", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        System.out.println("LoginFilter: Authentication successful for user " + authentication.getName());
        super.successfulAuthentication(request, response, chain, authentication);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Authentication Failed\", \"message\": \"" + failed.getMessage() + "\"}");
        System.out.println("LoginFilter: Authentication failed: " + failed.getMessage());
    }
}
