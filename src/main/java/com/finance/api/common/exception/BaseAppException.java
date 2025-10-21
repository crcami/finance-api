package com.finance.api.common.exception;

import org.springframework.http.HttpStatus;

/** Base application exception carrying an HTTP status and a stable error code. */
public abstract class BaseAppException extends RuntimeException {
  private final HttpStatus status;
  private final String code;

  protected BaseAppException(String message, HttpStatus status, String code) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public HttpStatus getStatus() { return status; }
  public String getCode() { return code; }
}
