package com.finance.api.user.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "app_user",
    uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = "email")
)
public class UserEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();


    @Column(name = "reset_token_sha256")
    private String resetTokenSha256;

    @Column(name = "reset_token_expires_at")
    private Instant resetTokenExpiresAt;

    @Column(name = "reset_token_used", nullable = false)
    private boolean resetTokenUsed = false;

    @Column(name = "reset_request_ip")
    private String resetRequestIp;

    @Column(name = "reset_user_agent")
    private String resetUserAgent;

    @Column(name = "reset_created_at", nullable = false)
    private Instant resetCreatedAt = Instant.now();

    @PreUpdate
    public void touch() {
        this.updatedAt = Instant.now();
    }



    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getResetTokenSha256() { return resetTokenSha256; }
    public void setResetTokenSha256(String resetTokenSha256) { this.resetTokenSha256 = resetTokenSha256; }

    public Instant getResetTokenExpiresAt() { return resetTokenExpiresAt; }
    public void setResetTokenExpiresAt(Instant resetTokenExpiresAt) { this.resetTokenExpiresAt = resetTokenExpiresAt; }

    public boolean isResetTokenUsed() { return resetTokenUsed; }
    public void setResetTokenUsed(boolean resetTokenUsed) { this.resetTokenUsed = resetTokenUsed; }

    public String getResetRequestIp() { return resetRequestIp; }
    public void setResetRequestIp(String resetRequestIp) { this.resetRequestIp = resetRequestIp; }

    public String getResetUserAgent() { return resetUserAgent; }
    public void setResetUserAgent(String resetUserAgent) { this.resetUserAgent = resetUserAgent; }

    public Instant getResetCreatedAt() { return resetCreatedAt; }
    public void setResetCreatedAt(Instant resetCreatedAt) { this.resetCreatedAt = resetCreatedAt; }
}
