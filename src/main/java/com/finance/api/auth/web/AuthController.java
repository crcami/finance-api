package com.finance.api.auth.web;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.api.auth.application.AuthService;
import com.finance.api.auth.application.PasswordReset;
import com.finance.api.auth.domain.LoginRequest;
import com.finance.api.auth.domain.LoginResponse;
import com.finance.api.auth.domain.PasswordNew;
import com.finance.api.auth.domain.RefreshRequest;
import com.finance.api.auth.domain.RegisterRequest;
import com.finance.api.auth.domain.SessionResponse;
import com.finance.api.common.api.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Register, login, refresh, logout and session check")
public class AuthController {

    private final AuthService authService;
    private final PasswordReset passwordReset;

    public AuthController(AuthService authService, PasswordReset passwordReset) {
        this.authService = authService;
        this.passwordReset = passwordReset;
    }

    @Operation(summary = "Register a new user (e-mail + password) and sign in")
    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(
            @Valid @RequestBody RegisterRequest in,
            @Parameter(hidden = true) HttpServletRequest http
    ) {
        var out = authService.register(in, http);
        return ApiResponse.ok("Registered", out);
    }

    @Operation(summary = "Authenticate with credentials and start a session")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @Valid @RequestBody LoginRequest in,
            @Parameter(hidden = true) HttpServletRequest http
    ) {
        var out = authService.login(in, http);
        return ApiResponse.ok("Authenticated", out);
    }

    @Operation(summary = "Refresh access token using a valid refresh token")
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(
            @Valid @RequestBody RefreshRequest in,
            @Parameter(hidden = true) HttpServletRequest http
    ) {
        var out = authService.refresh(in, http);
        return ApiResponse.ok("Refreshed", out);
    }

    @Operation(summary = "Logout current session (requires authentication)")
    @PostMapping("/logout")
    @SecurityRequirement(name = "bearer-jwt")
    public ApiResponse<Void> logout(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(hidden = true) HttpServletRequest http
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            return ApiResponse.fail("Unauthenticated");
        }
        authService.logout(userId, http);
        return ApiResponse.ok("Logged out", null);
    }

    @Operation(summary = "Return session status and current user id (if authenticated)")
    @GetMapping("/session")
    public ApiResponse<SessionResponse> session(
            @Parameter(hidden = true) Authentication authentication
    ) {
        boolean ok = authentication != null && authentication.getPrincipal() instanceof UUID;
        UUID userId = ok ? (UUID) authentication.getPrincipal() : null;
        return ApiResponse.ok(new SessionResponse(ok, userId));
    }

    @PermitAll
    @Operation(summary = "Public: send a temporary password to the user's e-mail")
    @PostMapping("/reset-password")
    @SecurityRequirements(value = {})
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordNew in) {
        passwordReset.resetAndSend(in);
        return ApiResponse.ok("Nova senha temporária enviada no email, após o login favor alterar novamente.", null);
    }
}
