package com.finance.api.auth.application;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.finance.api.config.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final Key key;
    private final String issuer;
    private final long accessTtl;
    private final long refreshTtl;

    public JwtService(JwtProperties props) {
        this.issuer = props.getIssuer();
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTtl = props.getAccess().getTtl();
        this.refreshTtl = props.getRefresh().getTtl();
    }

    public Jws<Claims> parse(String jwt) {
        return Jwts.parserBuilder()
                .requireIssuer(issuer)
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt);
    }

    public String generateAccess(UUID userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(accessTtl)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefresh(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setIssuer(issuer)
                .setSubject(userId.toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(refreshTtl)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
