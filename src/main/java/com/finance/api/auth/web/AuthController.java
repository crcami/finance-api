package com.finance.api.auth.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.api.auth.application.AuthService;
import com.finance.api.auth.domain.LoginRequest;
import com.finance.api.auth.domain.LoginResponse;
import com.finance.api.auth.domain.RefreshRequest;
import com.finance.api.auth.domain.SessionResponse;
import com.finance.api.common.api.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest in,
            HttpServletRequest http) {
        var out = authService.login(in, http);
        return ResponseEntity.ok(ApiResponse.ok("Authenticated", out));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshRequest in,
            HttpServletRequest http) {
        var out = authService.refresh(in, http);
        return ResponseEntity.ok(ApiResponse.ok("Refreshed", out));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication, HttpServletRequest http) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            return ResponseEntity.status(401).body(ApiResponse.fail("Unauthenticated"));
        }
        authService.logout(userId, http);
        return ResponseEntity.ok(ApiResponse.ok("Logged out", null));
    }


    @GetMapping("/session")
    public ApiResponse<SessionResponse> session(Authentication authentication) {
        boolean ok = authentication != null && authentication.getPrincipal() instanceof UUID;
        UUID userId = ok ? (UUID) authentication.getPrincipal() : null;
        return ApiResponse.ok(new SessionResponse(ok, userId));
    }
}
