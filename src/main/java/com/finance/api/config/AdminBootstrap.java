package com.finance.api.config;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.finance.api.user.persistence.UserEntity;
import com.finance.api.user.persistence.UserRepository;

@Component
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminBootstrap(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Value("${app.bootstrap.enabled:true}")
    private boolean enabled;

    @Value("${app.bootstrap.admin.email:admin@local}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.name:Admin}")
    private String adminName;

    @Value("${app.bootstrap.admin.password:ChangeMe123!}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) return;
       if (users.existsByEmailIgnoreCase(adminEmail.trim())) return;

        UserEntity u = new UserEntity();
        u.setEmail(adminEmail);
        u.setFullName(adminName);
        u.setPasswordHash(encoder.encode(adminPassword));
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());

        users.save(u);
        System.out.println("[BOOTSTRAP] Usuário inicial criado: " + adminEmail);
    }
}
