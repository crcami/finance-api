package com.finance.api.auth.persistence;

import java.time.Instant;
import java.util.UUID;


@Deprecated
public class PasswordResetTokenEntity {

    private UUID id;
    private UUID userId;
    private String tokenSha256;
    private Instant expiresAt;
    private boolean used;
    private String requestIp;
    private String userAgent;
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getTokenSha256() { return tokenSha256; }
    public void setTokenSha256(String tokenSha256) { this.tokenSha256 = tokenSha256; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String requestIp) { this.requestIp = requestIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
