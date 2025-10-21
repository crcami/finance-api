package com.finance.api.auth.domain;

import java.util.UUID;

public record TokenClaims(UUID userId, String email) {

}
