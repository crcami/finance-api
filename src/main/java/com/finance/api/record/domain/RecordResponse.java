package com.finance.api.record.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RecordResponse(
    UUID id,
    UUID categoryId,
    RecordKind kind,
    RecordStatus status,
    BigDecimal amount,
    LocalDate dueDate,
    Instant paidAt,
    String description
) { }
