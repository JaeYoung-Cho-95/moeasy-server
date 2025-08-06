package com.moeasy.moeasy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FailApiResponseDto {
    private final String status;
    private final Integer code;
    private final String message;
    private final LocalDateTime timestamp = LocalDateTime.now();

    public static  FailApiResponseDto fail(Integer code, String message) {
        return FailApiResponseDto.builder()
                .status("fail")
                .code(code)
                .message(message)
                .build();
    }
}
