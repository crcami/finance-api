package com.finance.api.user.persistence;

import java.time.Instant;            
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserEntity> findByResetTokenSha256AndResetTokenUsedFalseAndResetTokenExpiresAtAfter(
            String resetTokenSha256,
            Instant now
    );
}
