package com.moeasy.moeasy.controller;

import com.moeasy.moeasy.common.ErrorApiResponseDto;
import com.moeasy.moeasy.common.FailApiResponseDto;
import com.moeasy.moeasy.common.SuccessApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthcheckController {

    @GetMapping("/healthcheck")
    public ResponseEntity<SuccessApiResponseDto<String>> healthCheck() {
        return ResponseEntity.ok(
                SuccessApiResponseDto.success(
                        200,
                        "The server is operating normally",
                        "경우에 따라 Map || LIST 가 담길 예정"
                )
        );
    }

    @GetMapping("/fail")
    public ResponseEntity<FailApiResponseDto<Object>> getErrorResponse() {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        FailApiResponseDto.fail(
                                404,
                                "no resource"
                        )
                );
    }

    @GetMapping("/error")
    public ResponseEntity<ErrorApiResponseDto<Object>> getFailResponse() {
        ErrorApiResponseDto.ErrorResponse errorResponse = ErrorApiResponseDto.ErrorResponse.builder()
                .type("ValidationException")
                .errorDetail("server error.")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ErrorApiResponseDto.error(500, errorResponse)
                );
    }
}