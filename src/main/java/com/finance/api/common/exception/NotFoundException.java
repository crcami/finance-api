package com.finance.api.common.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseAppException {
  public NotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
  }
}
