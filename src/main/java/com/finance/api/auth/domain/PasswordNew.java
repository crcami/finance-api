package com.finance.api.auth.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public class PasswordNew {
  @NotBlank
  @Email
  private String email;

  public PasswordNew() {}
  public PasswordNew(String email) { this.email = email; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
}
