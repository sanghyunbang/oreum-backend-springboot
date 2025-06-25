package com.oreum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // URL이 /img/** 로 들어오면
        // 실제 파일 시스템의 C:/upload/img/ 에서 이미지 찾음
        registry.addResourceHandler("/img/**")
                .addResourceLocations("file:///C:/upload/img/");
    }
}
