package com.finance.api.record.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordRequest(
    @NotNull(message = "Kind is required") RecordKind kind,
    @NotNull(message = "Status is required") RecordStatus status,
    @NotNull(message = "Amount is required") @DecimalMin(value = "0.00") BigDecimal amount,
    @NotNull(message = "Due date is required") LocalDate dueDate,
    UUID categoryId,
    @Size(max = 255, message = "Description must be at most 255 characters") String description
) { }
