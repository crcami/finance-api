package com.finance.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Binds properties under 'security.jwt' from application.yml.
 * Example:
 * security:
 *   jwt:
 *     issuer: "finance-api"
 *     access:
 *       ttl: 900
 *     refresh:
 *       ttl: 2592000
 *     secret: ${JWT_SECRET:change-me-super-secret}
 */
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

  @NotBlank
  private String issuer;

  @NotBlank
  private String secret;

  private final Access access = new Access();
  private final Refresh refresh = new Refresh();

  public String getIssuer() { return issuer; }
  public void setIssuer(String issuer) { this.issuer = issuer; }

  public String getSecret() { return secret; }
  public void setSecret(String secret) { this.secret = secret; }

  public Access getAccess() { return access; }
  public Refresh getRefresh() { return refresh; }

  public static class Access {
    @Positive
    private long ttl; 
    public long getTtl() { return ttl; }
    public void setTtl(long ttl) { this.ttl = ttl; }
  }

  public static class Refresh {
    @Positive
    private long ttl; 
    public long getTtl() { return ttl; }
    public void setTtl(long ttl) { this.ttl = ttl; }
  }
}
