package com.moeasy.moeasy.common;

import com.moeasy.moeasy.controller.HealthcheckController;
import com.moeasy.moeasy.controller.account.AccountController;
import com.moeasy.moeasy.controller.question.QuestionController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(
        annotations = {
                RestController.class
        },
        basePackageClasses = {
            AccountController.class,
            QuestionController.class,
            HealthcheckController.class
        }
)
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApiResponseDto> handleServerException(Exception e) {
        log.error("서버 에러 발생 : " + e);
        ErrorApiResponseDto.ErrorResponse errorResponse = ErrorApiResponseDto.ErrorResponse.builder()
                .type(e.getClass().getSimpleName())
                .errorDetail("server error")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorApiResponseDto.error(500, errorResponse));
    }
}
