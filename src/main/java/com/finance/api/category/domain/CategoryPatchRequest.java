package com.finance.api.category.domain;

import jakarta.validation.constraints.NotNull;

public record CategoryPatchRequest(
    @NotNull(message = "Archived flag is required")
    Boolean archived
) { }
