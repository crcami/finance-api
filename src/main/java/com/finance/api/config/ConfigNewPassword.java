package com.finance.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.password-reset")
public class ConfigNewPassword {


  private int length = 12;

  private String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

  public int getLength() { return length; }
  public void setLength(int length) { this.length = length; }

  public String getAlphabet() { return alphabet; }
  public void setAlphabet(String alphabet) { this.alphabet = alphabet; }
}
