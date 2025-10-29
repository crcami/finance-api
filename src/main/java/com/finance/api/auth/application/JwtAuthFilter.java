package com.finance.api.auth.application;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            try {
                Jws<Claims> jws = jwtService.parse(token);
                String sub = jws.getBody().getSubject();
                if (sub != null && !sub.isBlank()) {
                    UUID userId = UUID.fromString(sub);
                    var auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignored) {
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String path = request.getServletPath(); 
        final String method = request.getMethod();

        boolean isDocs = path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui");
        boolean isOptions = "OPTIONS".equals(method);
        boolean isPublicAuth
                = ("POST".equals(method) && ("/auth/login".equals(path)
                || "/auth/register".equals(path)
                || "/auth/refresh".equals(path)
                || "/auth/logout".equals(path)
                || "/auth/reset-password".equals(path) 
                ))
                || ("GET".equals(method) && ("/auth/session".equals(path)
                || "/users/by-email".equals(path)));

        return isDocs || isOptions || isPublicAuth;
    }

}
