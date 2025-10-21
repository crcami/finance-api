package com.finance.api.common.exception;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.finance.api.common.api.ErrorEnvelope;
import com.finance.api.common.api.ErrorEnvelope.FieldErrorDetail;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseAppException.class)
    public ResponseEntity<ErrorEnvelope> handleBase(BaseAppException ex, org.springframework.web.context.request.WebRequest request) {
        var body = ErrorEnvelope.of(ex.getMessage(), ex.getCode(), getPath(request), List.of());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorEnvelope> handleNotFound(NotFoundException ex, org.springframework.web.context.request.WebRequest request) {
        var body = ErrorEnvelope.of(ex.getMessage(), ex.getCode(), getPath(request), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorEnvelope> handleBeanValidation(MethodArgumentNotValidException ex,
            org.springframework.web.context.request.WebRequest request) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldErrorDetail(e.getField(), e.getDefaultMessage()))
                .toList();
        var body = ErrorEnvelope.of("Validation failed", "VALIDATION_ERROR", getPath(request), errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorEnvelope> handleConstraint(ConstraintViolationException ex,
            org.springframework.web.context.request.WebRequest request) {
        var errors = ex.getConstraintViolations().stream()
                .map(v -> new FieldErrorDetail(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        var body = ErrorEnvelope.of("Validation failed", "VALIDATION_ERROR", getPath(request), errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorEnvelope> handleDataIntegrity(DataIntegrityViolationException ex,
            org.springframework.web.context.request.WebRequest request) {
        var body = ErrorEnvelope.of("Data integrity violation", "DATA_INTEGRITY", getPath(request), List.of());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorEnvelope> handleAccessDenied(AccessDeniedException ex,
            org.springframework.web.context.request.WebRequest request) {
        var body = ErrorEnvelope.of("Access denied", "FORBIDDEN", getPath(request), List.of());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorEnvelope> handleGeneric(Exception ex, org.springframework.web.context.request.WebRequest request) {
        var body = ErrorEnvelope.of("Unexpected error", "UNEXPECTED_ERROR", getPath(request), List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String getPath(org.springframework.web.context.request.WebRequest request) {
        var desc = request.getDescription(false); // like "uri=/path"
        return (desc != null && desc.startsWith("uri=")) ? desc.substring(4) : desc;
    }
}
