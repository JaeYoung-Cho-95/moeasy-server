package com.moeasy.moeasy.config.response.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

  @Schema(description = "HTTP 상태 코드", example = "40X | 50X")
  private final Integer code;

  @Schema(description = "응답 시간")
  private final LocalDateTime timestamp = LocalDateTime.now();

  @Schema(description = "에러 상세 내용")
  private final String message;

  public static ErrorResponseDto from(Integer code, String errorDetail) {
    return ErrorResponseDto.builder()
        .code(code)
        .message(errorDetail)
        .build();
  }
}