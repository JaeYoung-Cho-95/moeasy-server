package com.moeasy.moeasy.controller;

import com.moeasy.moeasy.config.response.responseDto.ErrorResponseDto;
import com.moeasy.moeasy.config.response.responseDto.FailResponseDto;
import com.moeasy.moeasy.config.response.responseDto.SuccessResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthcheckController {

  @GetMapping("/healthcheck")
  public ResponseEntity<SuccessResponseDto<String>> healthCheck() {
    return ResponseEntity.ok(
        SuccessResponseDto.success(
            200,
            "The server is operating normally",
            "경우에 따라 Map || LIST 가 담길 예정"
        )
    );
  }

  @GetMapping("/fail")
  public ResponseEntity<FailResponseDto> getErrorResponse() {
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(
            FailResponseDto.fail(
                404,
                "no resource"
            )
        );
  }

  @GetMapping("/error")
  public ResponseEntity<ErrorResponseDto> getFailResponse() {
    ErrorResponseDto.ErrorResponse errorResponse = ErrorResponseDto.ErrorResponse.builder()
        .type("ValidationException")
        .errorDetail("server error.")
        .build();

    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ErrorResponseDto.error(500, errorResponse)
        );
  }
}