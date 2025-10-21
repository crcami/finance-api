package com.finance.api.auth.domain;

import java.util.UUID;

public record LoginResponse(UUID userId, String email, JwtPair tokens) { }
