package com.finance.api.user.application;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.api.common.exception.NotFoundException;
import com.finance.api.user.domain.ChangePasswordRequest;
import com.finance.api.user.domain.UpdateProfileRequest;
import com.finance.api.user.domain.UserProfileResponse;
import com.finance.api.user.persistence.UserEntity;
import com.finance.api.user.persistence.UserRepository;

@Service
public class UserService {

  private final UserRepository users;
  private final PasswordEncoder encoder;

  public UserService(UserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  @Transactional(readOnly = true)
  public UserProfileResponse me(UUID userId) {
    var u = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    return toResponse(u);
  }

  @Transactional
  public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest in) {
    var u = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    u.setFullName(in.fullName());
    return toResponse(u);
  }

  @Transactional
  public void changePassword(UUID userId, ChangePasswordRequest in) {
    var u = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    if (!encoder.matches(in.currentPassword(), u.getPasswordHash())) {
      throw new IllegalArgumentException("Current password is invalid"); 
    }
    u.setPasswordHash(encoder.encode(in.newPassword()));
  }

  private static UserProfileResponse toResponse(UserEntity u) {
    return new UserProfileResponse(u.getId(), u.getEmail(), u.getFullName());
  }
}
