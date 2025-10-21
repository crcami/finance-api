package com.finance.api.common.api;

import java.time.Instant;
import java.util.List;


public record ErrorEnvelope(
    boolean success,
    String message,
    String code,
    String path,
    Instant timestamp,
    List<FieldErrorDetail> errors
) {
  public static ErrorEnvelope of(String message, String code, String path, List<FieldErrorDetail> errors) {
    return new ErrorEnvelope(false, message, code, path, Instant.now(), errors);
  }

  /** Field-level error detail used for validation messages. */
  public record FieldErrorDetail(String field, String error) {}
}
