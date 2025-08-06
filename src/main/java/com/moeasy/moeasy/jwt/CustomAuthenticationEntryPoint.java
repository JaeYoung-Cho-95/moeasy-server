package com.moeasy.moeasy.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeasy.moeasy.common.FailApiResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        FailApiResponseDto failResponse = FailApiResponseDto.fail(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication failed. Please ensure that the request includes a valid authentication token."
        );

        response.getWriter().write(objectMapper.writeValueAsString(failResponse));
    }
}
