package com.finance.api.user.web;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.api.common.api.ApiResponse;
import com.finance.api.user.application.UserService;
import com.finance.api.user.domain.ChangePasswordRequest;
import com.finance.api.user.domain.UpdateProfileRequest;
import com.finance.api.user.domain.UserProfileResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users/me")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

  private final UserService service;

  public UserController(UserService service) { this.service = service; }

  @Operation(summary = "Get current user's profile")
  @GetMapping
  public ApiResponse<UserProfileResponse> me(Authentication auth) {
    UUID userId = (UUID) auth.getPrincipal();
    return ApiResponse.ok(service.me(userId));
  }

  @Operation(summary = "Update current user's profile")
  @PutMapping
  public ApiResponse<UserProfileResponse> update(@Valid @RequestBody UpdateProfileRequest in,
                                                 Authentication auth) {
    UUID userId = (UUID) auth.getPrincipal();
    return ApiResponse.ok("Profile updated", service.updateProfile(userId, in));
  }

  @Operation(summary = "Change current user's password")
  @PutMapping("/password")
  public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest in,
                                          Authentication auth) {
    UUID userId = (UUID) auth.getPrincipal();
    service.changePassword(userId, in);
    return ApiResponse.ok("Password changed", null);
  }
}
