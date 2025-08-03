package com.moeasy.moeasy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessApiResponseDto<T> {
    private final String status;
    private final Integer code;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp = LocalDateTime.now();


    public static <T> SuccessApiResponseDto<T> success(Integer code, String message, T data) {
        return SuccessApiResponseDto.<T>builder()
                .status("success")
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
}
