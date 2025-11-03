package com.finance.api.auth.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.finance.api.auth.domain.ConfirmResetRequest;
import com.finance.api.auth.domain.ForgotPasswordRequest;
import com.finance.api.common.exception.NotFoundException;
import com.finance.api.user.persistence.UserEntity;
import com.finance.api.user.persistence.UserRepository;

@Service
public class PasswordResetService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailer;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.mail.from:${MAIL_FROM:${MAIL_USER}}}")
    private String mailFrom;

    private final Duration tokenTtl;
    private final String resetBaseUrl;

    public PasswordResetService(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailer
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.mailer = mailer;
        this.tokenTtl = Duration.ofMinutes(15);
        this.resetBaseUrl = System.getenv().getOrDefault("APP_BASE_URL", "http://localhost:3000");
    }

    @Transactional
    public void requestReset(ForgotPasswordRequest in, String ip, String userAgent) {
        Optional<UserEntity> maybeUser = users.findByEmailIgnoreCase(in.email());

        if (maybeUser.isEmpty()) return;

        UserEntity user = maybeUser.get();

        String plainToken = generateToken(32);
        String tokenSha256 = sha256Hex(plainToken);
        Instant expiresAt = Instant.now().plus(tokenTtl);

        user.setResetTokenSha256(tokenSha256);
        user.setResetTokenExpiresAt(expiresAt);
        user.setResetTokenUsed(false);
        user.setResetRequestIp(ip);
        user.setResetUserAgent(userAgent);
        user.setResetCreatedAt(Instant.now());
        users.save(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendResetEmail(user, plainToken);
            }
        });
    }

    @Transactional
    public void confirmReset(ConfirmResetRequest in) {
        String sha = sha256Hex(in.token());
        UserEntity user = users
            .findByResetTokenSha256AndResetTokenUsedFalseAndResetTokenExpiresAtAfter(sha, Instant.now())
            .orElseThrow(() -> new NotFoundException("Token inválido ou expirado"));

        user.setPasswordHash(passwordEncoder.encode(in.newPassword()));
        user.setResetTokenUsed(true);

       
    }

    private void sendResetEmail(UserEntity user, String token) {
        String url = resetBaseUrl + "/reset-password?token=" + token;

        String subject = "Redefinição de senha";
        String greeting = (user.getFullName() == null || user.getFullName().isBlank())
                ? "Olá" : "Olá " + user.getFullName();

        long minutes = tokenTtl.toMinutes();
        String ttlPt = minutes == 1 ? "1 minuto" : minutes + " minutos";

        String body = greeting + ",\n\n"
                + "Recebemos uma solicitação para redefinir a sua senha.\n"
                + "Se foi você, acesse o link abaixo e defina uma nova senha:\n\n"
                + url + "\n\n"
                + "Este link expira em " + ttlPt + ".\n"
                + "Se você não solicitou esta ação, pode ignorar este e-mail.\n\n"
                + "Equipe Finance";

        SimpleMailMessage msg = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            msg.setFrom(mailFrom);
        }
        msg.setTo(user.getEmail());
        msg.setSubject(subject);
        msg.setText(body);
        mailer.send(msg);
    }

    private String generateToken(int numBytes) {
        byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash token", e);
        }
    }
}
