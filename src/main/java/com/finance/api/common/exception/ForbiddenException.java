package com.finance.api.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseAppException {
  public ForbiddenException(String message) {
    super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
  }
}

