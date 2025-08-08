package com.moeasy.moeasy.response;

import com.moeasy.moeasy.controller.HealthcheckController;
import com.moeasy.moeasy.controller.account.AccountController;
import com.moeasy.moeasy.controller.question.QuestionController;
import com.moeasy.moeasy.response.custom.CustomFailException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @ExceptionHandler(CustomFailException.class)
    public ResponseEntity<FailApiResponseDto> handleCustomFailException(CustomFailException e) {
        log.error("CustomFailException 발생: " + e.getMessage());
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(FailApiResponseDto.fail(e.getCode(), e.getMessage()));
    }

    /**
     * JSON 파싱 오류 등 잘못된 형식의 요청을 처리합니다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<FailApiResponseDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("잘못된 형식의 JSON 요청: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(FailApiResponseDto.fail(HttpStatus.BAD_REQUEST.value(), "요청 형식이 올바르지 않습니다."));
    }
}