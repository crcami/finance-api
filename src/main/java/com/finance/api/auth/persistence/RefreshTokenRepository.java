package com.finance.api.auth.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByUserIdAndTokenHashAndRevokedFalseAndExpiresAtAfter(
            UUID userId, String tokenHash, Instant now);

    List<RefreshTokenEntity> findAllByUserIdAndUserAgentAndRevokedFalse(UUID userId, String userAgent);

    List<RefreshTokenEntity> findAllByUserIdAndRevokedFalse(UUID userId);
}
