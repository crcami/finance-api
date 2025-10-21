package com.finance.api.user.domain;

import java.util.UUID;

public record UserProfileResponse(UUID id, String email, String fullName) { }
