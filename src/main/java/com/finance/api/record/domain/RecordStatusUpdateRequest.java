package com.finance.api.record.domain;

import jakarta.validation.constraints.NotNull;

public record RecordStatusUpdateRequest(@NotNull RecordStatus status) {}
