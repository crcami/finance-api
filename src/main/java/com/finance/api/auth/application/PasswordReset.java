package com.finance.api.auth.application;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.finance.api.auth.domain.PasswordNew;
import com.finance.api.common.exception.NotFoundException;
import com.finance.api.config.ConfigNewPassword;
import com.finance.api.user.persistence.UserEntity;
import com.finance.api.user.persistence.UserRepository;

@Service
public class PasswordReset {

    private static final Logger log = LoggerFactory.getLogger(PasswordReset.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JavaMailSender mailer;
    private final ConfigNewPassword config;
    private final SecureRandom random = new SecureRandom();

    public PasswordReset(
            UserRepository users,
            PasswordEncoder encoder,
            JavaMailSender mailer,
            ConfigNewPassword config
    ) {
        this.users = users;
        this.encoder = encoder;
        this.mailer = mailer;
        this.config = config;
    }

    @Transactional
    public void resetAndSend(PasswordNew in) {
        UserEntity user = users.findByEmailIgnoreCase(in.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));

        String plainTemp = generateTempPassword(config.getLength(), config.getAlphabet());
        user.setPasswordHash(encoder.encode(plainTemp));
        users.save(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    sendEmail(user, plainTemp);
                } catch (MailException e) {
                    log.error("Failed to send password reset e-mail to {}: {}", user.getEmail(), e.getMessage(), e);
                }
            }
        });
    }

    private String generateTempPassword(int length, String alphabet) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    private void sendEmail(UserEntity user, String tempPassword) {
        String subject = "Recuperação de senha";
        String text = "Olá " + (user.getFullName() == null ? "" : user.getFullName()) + ",\n\n"
                + "Sua nova senha temporária é: " + tempPassword + "\n"
                + "Por favor, altere-a após o primeiro login.\n\n"
                + "Equipe Finance";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());
        msg.setSubject(subject);
        msg.setText(text);
        mailer.send(msg);
    }
}
