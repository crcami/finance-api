package com.finance.api.user.application;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.api.auth.persistence.RefreshTokenEntity;
import com.finance.api.auth.persistence.RefreshTokenRepository;
import com.finance.api.common.exception.BadRequestException;
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
  private final RefreshTokenRepository refreshTokens; 

  public UserService(UserRepository users, PasswordEncoder encoder,
                     RefreshTokenRepository refreshTokens) { 
    this.users = users;
    this.encoder = encoder;
    this.refreshTokens = refreshTokens; 
  }

  @Transactional(readOnly = true)
  public UserProfileResponse me(UUID userId) {
    var u = users.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    return toResponse(u);
  }

  @Transactional
  public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest in) {
    var u = users.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    String name = in.fullName() == null ? "" : in.fullName().trim();
    u.setFullName(name.isBlank() ? u.getFullName() : name);
    return toResponse(u);
  }

  @Transactional
  public void changePassword(UUID userId, ChangePasswordRequest in) {
    var u = users.findById(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

    if (!encoder.matches(in.currentPassword(), u.getPasswordHash())) {
      throw new BadRequestException("Senha atual inválida");
    }

    u.setPasswordHash(encoder.encode(in.newPassword()));

    revokeAllRefreshTokens(userId);
  }

  private void revokeAllRefreshTokens(UUID userId) {
    List<RefreshTokenEntity> list = refreshTokens.findAllByUserIdAndRevokedFalse(userId);
    if (!list.isEmpty()) {
      for (var rt : list) {
        rt.setRevoked(true);
      }
      refreshTokens.saveAll(list);
    }
  }

  private static UserProfileResponse toResponse(UserEntity u) {
    return new UserProfileResponse(u.getId(), u.getEmail(), u.getFullName());
  }

  
  @Transactional(readOnly = true)
  public String findFullNameByEmail(String email) {
    var u = users.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    return u.getFullName();
  }
}
