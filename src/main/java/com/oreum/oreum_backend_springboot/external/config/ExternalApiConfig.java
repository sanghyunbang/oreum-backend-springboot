package com.oreum.oreum_backend_springboot.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.time.Duration;

@Configuration
public class ExternalApiConfig {

    @Bean
    public RestTemplate restTemplate() {
        var factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 단위: 밀리초
        factory.setReadTimeout(5000);    // 단위: 밀리초

        return new RestTemplate(factory);
    }
}
