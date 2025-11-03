package com.finance.api.auth.domain;

import jakarta.validation.constraints.NotBlank;

public record ConfirmResetRequest(
        @NotBlank String token,
        @NotBlank String newPassword
) {}
