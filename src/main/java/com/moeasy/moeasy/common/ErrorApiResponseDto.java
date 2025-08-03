package com.moeasy.moeasy.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorApiResponseDto<T> {
    private final String status;
    private final Integer code;
    private final ErrorResponse error;
    private final LocalDateTime timestamp = LocalDateTime.now();


    public static <T> ErrorApiResponseDto<T> error(Integer code, ErrorResponse errorResponse) {
        return ErrorApiResponseDto.<T>builder()
                .status("error")
                .code(code)
                .error(errorResponse)
                .build();
    }


    @Getter
    @Builder
    public static class ErrorResponse {
        private String type;
        private String errorDetail;
    }
}
