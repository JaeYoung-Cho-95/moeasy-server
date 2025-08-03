package com.moeasy.moeasy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Swagger UI, API docs, health check 등 인증 없이 접근을 허용할 경로 목록
    private static final String[] PERMIT_ALL_PATTERNS = {
            // Health Check API
            "/api/healthcheck", "/api/fail", "/api/error",

            // Swagger UI 리소스
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",

            // 만약 application.properties 등에서 swagger 경로를 커스텀했다면 해당 경로를 추가해야 합니다.
            // 예: springdoc.swagger-ui.path=/api/moiz/swagger
            "/api/moiz/swagger/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                    // 1. 인증 없이 허용할 경로들
                    .requestMatchers(PERMIT_ALL_PATTERNS).permitAll()

                    // 2. 인증이 필요한 API
                    .requestMatchers("/api/mypage/**", "/api/orders/**").authenticated()

                    // 3. 관리자(ADMIN) 역할이 필요한 API
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // 4. 위 경로를 제외한 나머지 모든 요청은 인증 필요
                    .anyRequest().authenticated()
            );
        return http.build();
    }
}