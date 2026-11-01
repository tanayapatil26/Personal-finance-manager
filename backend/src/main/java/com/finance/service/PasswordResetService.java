package com.finance.service;

import com.finance.model.AppUser;
import com.finance.model.PasswordResetToken;
import com.finance.repository.PasswordResetTokenRepository;
import com.finance.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.mail.from}")
    private String senderEmail;

    public PasswordResetService(
            UserRepository users,
            PasswordResetTokenRepository tokens,
            PasswordEncoder encoder,
            JavaMailSender mailSender) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public void requestReset(String email) {
        users.findByEmail(email.trim().toLowerCase()).ifPresent(user -> {
            // Remove previous reset tokens for this user.
            tokens.deleteByUserId(user.getId());

            byte[] randomBytes = new byte[32];
            secureRandom.nextBytes(randomBytes);

            String rawToken = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(randomBytes);

            String tokenHash = sha256(rawToken);

            PasswordResetToken resetToken = new PasswordResetToken(
                    tokenHash,
                    user,
                    Instant.now().plus(15, ChronoUnit.MINUTES));

            tokens.save(resetToken);

            String resetLink = frontendUrl.replaceAll("/+$", "")
                    + "/reset-password?token=" + rawToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderEmail);
            message.setTo(user.getEmail());
            message.setSubject("Reset your Personal Finance Manager password");
            message.setText(
                    "We received a request to reset your password.\n\n"
                            + "Open this link within 15 minutes to choose a new password:\n"
                            + resetLink + "\n\n"
                            + "If you did not request this, you can ignore this email.");

            mailSender.send(message);
        });
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = sha256(rawToken);

        PasswordResetToken resetToken = tokens.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "The reset link is invalid or has expired."));

        if (resetToken.isUsed() || !resetToken.getExpiresAt().isAfter(Instant.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The reset link is invalid or has expired.");
        }

        AppUser user = resetToken.getUser();
        user.setPasswordHash(encoder.encode(newPassword));

        resetToken.setUsed(true);

        users.save(user);
        tokens.save(resetToken);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to process reset token.", ex);
        }
    }
}