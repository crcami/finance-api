package com.finance.api.category.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 60, message = "Name must be at most 60 characters")
    String name,

    @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "Color must be HEX like #AABBCC")
    String color
) { }
