package com.finance.api.common.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseAppException {
  public BadRequestException(String message) {
    super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
  }
}
