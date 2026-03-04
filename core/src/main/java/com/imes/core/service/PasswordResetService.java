package com.imes.core.service;

import com.imes.infra.entity.PasswordResetTokenEntity;
import com.imes.infra.entity.UserEntity;
import com.imes.infra.repository.PasswordResetTokenRepository;
import com.imes.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Password Reset Service
 * Handles forgot password, reset password, and change password functionality
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordValidationService passwordValidationService;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRATION_HOURS = 1;

    /**
     * Initiate forgot password process - generate token and send email
     */
    @Transactional
    public void initiateForgotPassword(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        if (!user.getIsActive()) {
            throw new RuntimeException("User account is inactive");
        }

        // Check if user already has a valid token
        if (tokenRepository.hasValidToken(user.getId(), LocalDateTime.now())) {
            throw new RuntimeException("Password reset email already sent. Please check your inbox or wait before requesting again.");
        }

        // Generate token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);

        PasswordResetTokenEntity resetToken = PasswordResetTokenEntity.builder()
                .userId(user.getId())
                .token(token)
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(resetToken);

        // Send email
        emailService.sendPasswordResetEmail(user.getEmail(), token, user.getFullName());

        log.info("[PASSWORD-RESET] Reset token generated for user: {}", email);
    }

    /**
     * Reset password using token
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Validate new password
        PasswordValidationService.PasswordValidationResult validationResult =
                passwordValidationService.validate(newPassword);

        if (!validationResult.isValid()) {
            throw new RuntimeException("Invalid password: " + validationResult.getErrorMessage());
        }

        // Find token
        PasswordResetTokenEntity resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        // Validate token
        if (!resetToken.isValid()) {
            if (resetToken.isExpired()) {
                throw new RuntimeException("Password reset token has expired");
            }
            if (resetToken.isUsed()) {
                throw new RuntimeException("Password reset token has already been used");
            }
        }

        // Get user and update password
        UserEntity user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        // Delete all other tokens for this user
        tokenRepository.deleteAllUserTokens(user.getId());

        log.info("[PASSWORD-RESET] Password reset successfully for user: {}", user.getEmail());
    }

    /**
     * Change password for authenticated user
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        // Validate new password
        PasswordValidationService.PasswordValidationResult validationResult =
                passwordValidationService.validate(newPassword);

        if (!validationResult.isValid()) {
            throw new RuntimeException("Invalid password: " + validationResult.getErrorMessage());
        }

        // Get user
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Check if new password is same as current
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("[PASSWORD-CHANGE] Password changed successfully for user: {}", user.getEmail());
    }

    /**
     * Scheduled task to clean up expired tokens
     * Runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deleted > 0) {
            log.info("[PASSWORD-RESET] Cleaned up {} expired tokens", deleted);
        }
    }
}
