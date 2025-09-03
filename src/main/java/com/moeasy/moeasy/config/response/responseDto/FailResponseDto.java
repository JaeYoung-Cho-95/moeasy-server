package com.moeasy.moeasy.config.response.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FailResponseDto {

  @Schema(description = "40x 응답은 모두 fail", example = "fail")
  private final String status;

  @Schema(description = "HTTP 상태 코드", example = "400")
  private final Integer code;

  @Schema(description = "실패 메시지", example = "id 값의 데이터가 존재하지 않습니다.")
  private final String message;

  @Schema(description = "응답 시간")
  private final LocalDateTime timestamp = LocalDateTime.now();

  public static FailResponseDto from(Integer code, String message) {
    return FailResponseDto.builder()
        .status("fail")
        .code(code)
        .message(message)
        .build();
  }
}