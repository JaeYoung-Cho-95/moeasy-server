package com.moeasy.moeasy.config.response.responseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDto {

  @Schema(description = "50x 응답은 모두 error", example = "error")
  private final String status;

  @Schema(description = "HTTP 상태 코드", example = "50X")
  private final Integer code;

  @Schema(description = "에러 상세 내용")
  private final ErrorResponse error;

  @Schema(description = "응답 시간")
  private final LocalDateTime timestamp = LocalDateTime.now();


  public static ErrorResponseDto from(Integer code, String type, String errorDetail) {
    return ErrorResponseDto.builder()
        .status("error")
        .code(code)
        .error(ErrorResponse.builder()
            .type(type)
            .errorDetail(errorDetail)
            .build()
        )
        .build();
  }

  @Getter
  @Builder
  @Schema(description = "에러 응답 상세 정보")
  public static class ErrorResponse {

    @Schema(description = "에러 타입 (예외 클래스 이름)", example = "NullPointerException")
    private String type;
    @Schema(description = "에러 상세 메시지", example = "server error")
    private String errorDetail;
  }
}