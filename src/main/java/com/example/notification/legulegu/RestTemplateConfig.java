package com.example.notification.legulegu;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class RestTemplateConfig {

//    @Bean
//    public RestTemplate restTemplate(RestTemplateBuilder builder) {
//        return builder
//                .setConnectTimeout(Duration.ofSeconds(5))
//                .setReadTimeout(Duration.ofSeconds(30))
//                .build();
//    }
}
