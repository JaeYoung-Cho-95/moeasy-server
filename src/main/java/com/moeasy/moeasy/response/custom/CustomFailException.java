package com.moeasy.moeasy.response.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomFailException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;
    private final Integer code;

    public CustomFailException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.message = message;
        this.code = httpStatus.value();
    }
}
