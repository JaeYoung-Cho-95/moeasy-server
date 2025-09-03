package com.moeasy.moeasy.config.response.enums;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

  String name();

  HttpStatus getHttpStatus();

  String getMessage();
}
