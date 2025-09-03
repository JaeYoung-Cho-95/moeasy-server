package com.moeasy.moeasy.config.response.custom;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@Builder
public class CustomErrorException extends RuntimeException {

  private final Integer code;
  private final String message;

  public static CustomErrorException from(HttpStatus httpStatus, String message) {
    return CustomErrorException.builder()
        .message(message)
        .code(httpStatus.value())
        .build();
  }
}
