package com.guatequeriders.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class WebConfig {

    private final List<String> allowedOrigins;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(@Value("${app.allowed-origins:http://localhost:5173}") String allowedOrigins,
                     @Value("${app.environment:production}") String applicationEnvironment,
                     RateLimitInterceptor rateLimitInterceptor) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
        if (this.allowedOrigins.isEmpty() || this.allowedOrigins.stream().anyMatch("*"::equals)) {
            throw new IllegalStateException("APP_ALLOWED_ORIGINS must contain explicit origins");
        }
        if ("production".equalsIgnoreCase(applicationEnvironment)
                && this.allowedOrigins.stream().anyMatch(origin ->
                origin.contains("localhost") || origin.contains("127.0.0.1") || !origin.startsWith("https://"))) {
            throw new IllegalStateException("APP_ALLOWED_ORIGINS must contain HTTPS production origins");
        }
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(allowedOrigins.toArray(String[]::new))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "Accept")
                        .exposedHeaders("Retry-After")
                        .allowCredentials(false);
            }

            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(rateLimitInterceptor);
            }
        };
    }
}
