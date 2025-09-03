package com.moeasy.moeasy.config.response;

import com.moeasy.moeasy.config.response.custom.CustomErrorException;
import com.moeasy.moeasy.config.response.responseDto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleServerException(Exception e) {
    log.error("서버 에러 발생 : {}", e.getMessage(), e);
    ErrorResponseDto body = ErrorResponseDto.from(
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "server error"
    );
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  @ExceptionHandler(CustomErrorException.class)
  public ResponseEntity<ErrorResponseDto> handleCustomFailException(CustomErrorException e) {
    log.error("CustomFailException 발생: {}", e.getMessage(), e);
    ErrorResponseDto body = ErrorResponseDto.from(e.getCode(), e.getMessage());
    return ResponseEntity.status(e.getCode()).body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    log.warn("잘못된 형식의 JSON 요청: {}", e.getMessage(), e);
    ErrorResponseDto body = ErrorResponseDto.from(HttpStatus.BAD_REQUEST.value(),
        "요청 형식이 올바르지 않습니다.");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }
}