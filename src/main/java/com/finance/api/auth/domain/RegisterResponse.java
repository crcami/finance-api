package com.finance.api.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegisterResponse", description = "Result of a successful registration")
public record RegisterResponse(
    @Schema(example = "User created successfully") String message
) {}
