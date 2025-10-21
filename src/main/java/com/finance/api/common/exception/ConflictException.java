package com.finance.api.common.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseAppException {
  public ConflictException(String message) {
    super(message, HttpStatus.CONFLICT, "CONFLICT");
  }
}
