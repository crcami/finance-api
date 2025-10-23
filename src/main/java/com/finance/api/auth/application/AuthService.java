package com.finance.api.auth.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.api.auth.domain.JwtPair;
import com.finance.api.auth.domain.LoginRequest;
import com.finance.api.auth.domain.LoginResponse;
import com.finance.api.auth.domain.RefreshRequest;
import com.finance.api.auth.domain.RegisterRequest; // <-- NEW
import com.finance.api.auth.persistence.RefreshTokenEntity;
import com.finance.api.auth.persistence.RefreshTokenRepository;
import com.finance.api.common.exception.BadRequestException;
import com.finance.api.common.exception.UnauthorizedException;
import com.finance.api.user.persistence.UserEntity;
import com.finance.api.user.persistence.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthService {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users,
            RefreshTokenRepository refreshTokens,
            PasswordEncoder encoder,
            JwtService jwt) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public LoginResponse register(RegisterRequest in, HttpServletRequest http) {
        users.findByEmailIgnoreCase(in.email())
                .ifPresent(u -> {
                    throw new BadRequestException("E-mail already in use");
                });

        UserEntity user = new UserEntity();
        user.setEmail(in.email().trim().toLowerCase());
        user.setPasswordHash(encoder.encode(in.password()));
        user = users.save(user);

        String access = jwt.generateAccess(user.getId(), user.getEmail());
        String refresh = jwt.generateRefresh(user.getId());
        persistRefreshToken(user.getId(), refresh, http);

        return new LoginResponse(user.getId(), user.getEmail(), new JwtPair(access, refresh));
    }

    @Transactional
    public LoginResponse login(LoginRequest in, HttpServletRequest http) {
        UserEntity user = users.findByEmailIgnoreCase(in.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!encoder.matches(in.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String access = jwt.generateAccess(user.getId(), user.getEmail());
        String refresh = jwt.generateRefresh(user.getId());

        persistRefreshToken(user.getId(), refresh, http);

        return new LoginResponse(user.getId(), user.getEmail(), new JwtPair(access, refresh));
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest in, HttpServletRequest http) {
        Jws<Claims> jws = jwt.parse(in.refreshToken());
        UUID userId = UUID.fromString(jws.getBody().getSubject());
        Instant exp = jws.getBody().getExpiration().toInstant();

        String tokenHash = sha256(in.refreshToken());
        var opt = refreshTokens.findByUserIdAndTokenHashAndRevokedFalseAndExpiresAtAfter(
                userId, tokenHash, Instant.now());
        if (opt.isEmpty()) {
            throw new UnauthorizedException("Refresh token is invalid or revoked");
        }

        RefreshTokenEntity current = opt.get();
        current.setRevoked(true);
        refreshTokens.save(current);

        String access = jwt.generateAccess(userId, jws.getBody().get("email", String.class));
        String newRefresh = jwt.generateRefresh(userId);
        persistRefreshToken(userId, newRefresh, http);

        return new LoginResponse(userId, jws.getBody().get("email", String.class),
                new JwtPair(access, newRefresh));
    }

    @Transactional
    public void logout(UUID userId, HttpServletRequest http) {
        String userAgent = headerOrEmpty(http, "User-Agent");
        List<RefreshTokenEntity> list
                = refreshTokens.findAllByUserIdAndUserAgentAndRevokedFalse(userId, userAgent);
        for (var rt : list) {
            rt.setRevoked(true);
        }
        refreshTokens.saveAll(list);
    }

    private void persistRefreshToken(UUID userId, String refreshToken, HttpServletRequest http) {
        var entity = new RefreshTokenEntity();
        entity.setUserId(userId);
        entity.setTokenHash(sha256(refreshToken));
        entity.setUserAgent(headerOrEmpty(http, "User-Agent"));
        entity.setIpAddress(remoteIp(http));
        entity.setExpiresAt(jwt.parse(refreshToken).getBody().getExpiration().toInstant());
        entity.setRevoked(false);
        refreshTokens.save(entity);
    }

    private static String headerOrEmpty(HttpServletRequest http, String name) {
        String h = http.getHeader(name);
        return h == null ? "" : h;
    }

    private static String remoteIp(HttpServletRequest http) {
        String xff = http.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return http.getRemoteAddr();
    }

    private static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new BadRequestException("Unable to hash token");
        }
    }
}
