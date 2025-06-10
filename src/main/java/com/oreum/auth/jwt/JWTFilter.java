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

        //1. Authorization 헤더에서 코튼 찾기
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            token = authHeader.substring(7); // "Bearer " 제거하기
            System.out.println("Token from Authorization header: "+ token);
        }

        //2. 헤더가 없으면 쿠키에서 찾기(fallback)

        if(token == null){
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    System.out.println("Cookie: " + cookie.getName() + " = " + cookie.getValue());
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                    }
                }
                System.out.println("Extracted accessToken: " + token);    
            }
        }

        if (token == null) {
            System.out.println("No acessToken found in cookies");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (jwtUtil.isExpired(token)){
                System.out.println("Token is expired");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token expired!");
            }
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
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
