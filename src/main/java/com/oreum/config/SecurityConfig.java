package com.oreum.config;

import com.oreum.auth.jwt.JWTFilter;
import com.oreum.auth.jwt.JWTUtil;
import com.oreum.auth.jwt.LoginFilter;
import com.oreum.auth.jwt.AppCustomSuccessHandler;
import com.oreum.auth.jwt.CustomSuccessHandler;
import com.oreum.auth.jwt.CustomOAuth2FailureHandler;
import com.oreum.auth.service.CustomOAuth2UserService;
import com.oreum.auth.service.AppUserDetailsService; // AppUserDetailsService 임포트 추가

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider; // AuthenticationProvider 임포트
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // DaoAuthenticationProvider 임포트
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final AppCustomSuccessHandler appCustomSuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
    private final AppUserDetailsService appUserDetailsService; // AppUserDetailsService 주입

    // 모든 필요한 의존성들을 생성자를 통해 주입받습니다.
    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
                             JWTUtil jwtUtil,
                             AppCustomSuccessHandler appCustomSuccessHandler,
                             CustomOAuth2UserService customOAuth2UserService,
                             CustomSuccessHandler customSuccessHandler,
                             CustomOAuth2FailureHandler customOAuth2FailureHandler,
                             AppUserDetailsService appUserDetailsService) { // AppUserDetailsService 생성자 주입
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.appCustomSuccessHandler = appCustomSuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customSuccessHandler = customSuccessHandler;
        this.customOAuth2FailureHandler = customOAuth2FailureHandler;
        this.appUserDetailsService = appUserDetailsService; // 초기화
    }

    // 비밀번호 암호화를 위한 BCryptPasswordEncoder 빈 등록
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // DaoAuthenticationProvider 빈 등록
    // 이 프로바이더는 UserDetailsService와 PasswordEncoder를 사용하여 사용자 인증을 처리합니다.
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(appUserDetailsService); // 우리가 만든 AppUserDetailsService 설정
        authProvider.setPasswordEncoder(bCryptPasswordEncoder()); // BCryptPasswordEncoder 설정
        return authProvider;
    }

    // CORS (Cross-Origin Resource Sharing) 설정을 위한 빈 등록
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080", "http://10.0.2.2:3000", "http://10.0.2.2:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // HTTP 보안 설정을 정의하는 SecurityFilterChain 빈
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http	
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(auth -> auth.disable())
                .formLogin(auth -> auth.disable())
                .httpBasic(auth -> auth.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // DaoAuthenticationProvider를 AuthenticationManager에 추가
                .authenticationProvider(authenticationProvider()) // DaoAuthenticationProvider 추가
                
                .exceptionHandling(exception -> exception
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Authentication required or failed.\"}");
                    })
                )
                
                .oauth2Login(oauth -> oauth
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                    .successHandler(customSuccessHandler)
                    .failureHandler(customOAuth2FailureHandler)
                );

        LoginFilter loginFilter = new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil);
        loginFilter.setFilterProcessesUrl("/login");
        loginFilter.setAuthenticationSuccessHandler(appCustomSuccessHandler);

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                            "/",
                            "/login",
                            "/api/app_user/register", // 이 경로가 `UserController.java`의 `/register`와 일치하는지 확인해주세요.
                            "/oauth2/**",
                            "/api/user",
                            "/api/**",
                            "/public/**",
                            "/api/community/**", "/community/**",
                            "/api/posts/**", "/posts/**",
                            "/posts/*/comments"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new JWTFilter(jwtUtil), LoginFilter.class);

        return http.build();
    }
}