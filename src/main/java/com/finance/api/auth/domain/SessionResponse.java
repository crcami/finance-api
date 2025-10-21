package com.finance.api.auth.domain;

import java.util.UUID;

public record SessionResponse(boolean authenticated, UUID userId) { }
