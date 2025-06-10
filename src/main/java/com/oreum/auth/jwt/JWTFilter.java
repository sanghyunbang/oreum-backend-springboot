package com.oreum.auth.jwt;

import com.oreum.auth.dto.CustomOAuth2User;
import com.oreum.auth.dto.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("🍪 Cookie: " + cookie.getName() + " = " + cookie.getValue());
                if ("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }

        System.out.println("🔍 Extracted accessToken: " + token);

        if (token == null || jwtUtil.isExpired(token)) {
            System.out.println("⚠️ Token is null or expired");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtUtil.getUsername(token);
            String role = jwtUtil.getRole(token);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setRole(role);

            CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    customOAuth2User, null, customOAuth2User.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authToken);
            System.out.println("최종인증객체: "+ SecurityContextHolder.getContext().getAuthentication());
            System.out.println("✅ Authentication successful for: " + username);
        } catch (Exception e) {
            System.out.println("❌ Error parsing JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
