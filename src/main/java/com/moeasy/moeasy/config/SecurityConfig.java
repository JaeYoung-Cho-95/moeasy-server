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
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                    // 1. 인증이 필요한 API (특정 경로들)
                    .requestMatchers("/api/mypage/**", "/api/orders/**").authenticated()

                    // 2. 관리자(ADMIN) 역할이 필요한 API
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // 3. Swagger 경로는 개발 환경에서만 모두 허용
                    .requestMatchers("/api-docs/**", "/swagger-ui/**").permitAll()

                    // 4. 위에서 지정한 경로 외 "나머지 모든" 요청은 인증 없이 허용
                    .anyRequest().permitAll()
            );
        // .formLogin(withDefaults()); // .anyRequest().permitAll()을 사용하면 보통 formLogin은 필요 없거나 다르게 구성합니다.
        return http.build();
    }
}
