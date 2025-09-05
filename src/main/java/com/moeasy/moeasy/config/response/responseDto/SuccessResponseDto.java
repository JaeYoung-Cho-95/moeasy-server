package com.moeasy.moeasy.config.response.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponseDto<T> {

  @Schema(description = "20x 응답은 모두 success", example = "success")
  private final String status;

  @Schema(description = "HTTP 상태 코드", example = "200")
  private final Integer code;

  @Schema(description = "응답 메시지", example = "토큰 생성에 성공했습니다.")
  private final String message;

  @Schema(description = "응답 데이터")
  private final T data;

  @Schema(description = "응답 timestamp")
  private final LocalDateTime timestamp = LocalDateTime.now();


  public static <T> SuccessResponseDto<T> success(Integer code, String message, T data) {
    return SuccessResponseDto.<T>builder()
        .status("success")
        .code(code)
        .message(message)
        .data(data)
        .build();
  }
}