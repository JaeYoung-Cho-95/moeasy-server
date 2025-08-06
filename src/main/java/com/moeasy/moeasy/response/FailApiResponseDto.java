package com.moeasy.moeasy.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FailApiResponseDto {
    @Schema(description = "40x 응답은 모두 fail", example = "fail")
    private final String status;

    @Schema(description = "HTTP 상태 코드", example = "400")
    private final Integer code;

    @Schema(description = "실패 메시지", example = "id 값의 데이터가 존재하지 않습니다.")
    private final String message;

    @Schema(description = "응답 시간")
    private final LocalDateTime timestamp = LocalDateTime.now();

    public static  FailApiResponseDto fail(Integer code, String message) {
        return FailApiResponseDto.builder()
                .status("fail")
                .code(code)
                .message(message)
                .build();
    }
}