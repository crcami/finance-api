package com.finance.api.auth.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.api.auth.application.PasswordResetService;
import com.finance.api.auth.domain.ConfirmResetRequest;
import com.finance.api.auth.domain.ForgotPasswordRequest;
import com.finance.api.common.api.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;   
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Tag(name = "Password Reset", description = "Password reset flow") 
public class PasswordResetController {

    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset link via e-mail")
    @SecurityRequirements(value = {})
    public ApiResponse<Void> forgot(@Valid @RequestBody ForgotPasswordRequest in, HttpServletRequest http) {
        String ip = http.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = http.getRemoteAddr();
        String ua = http.getHeader("User-Agent");
        service.requestReset(in, ip, ua);
        return ApiResponse.ok("Se o e-mail existir, enviamos as instruções.", null);
    }

    @PostMapping("/forgot-password/confirm")
    @Operation(summary = "Confirm reset using the token and set a new password")
    @SecurityRequirements(value = {})
    public ApiResponse<Void> confirm(@Valid @RequestBody ConfirmResetRequest in) {
        service.confirmReset(in);
        return ApiResponse.ok("Senha alterada com sucesso.", null);
    }
}
