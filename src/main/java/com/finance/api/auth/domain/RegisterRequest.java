package com.finance.api.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "RegisterRequest", description = "Payload to create a new user account")
public record RegisterRequest(
    @Schema(description = "User e-mail (unique)", example = "cami@example.com")
    @NotBlank @Email String email,

    @Schema(description = "Strong password (min 8 chars, mixed case, number, symbol)", example = "Str0ng!Pass1")
    @NotBlank @Size(min = 8, max = 72) String password
) {}
