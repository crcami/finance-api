package com.finance.api.category.domain;

import java.util.UUID;

public record CategoryResponse(UUID id, String name, String color, boolean archived) { }
