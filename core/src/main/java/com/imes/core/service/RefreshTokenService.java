package com.imes.core.service;

import com.imes.infra.entity.RefreshTokenEntity;
import com.imes.infra.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing JWT refresh tokens
 * Handles token generation, validation, rotation, and revocation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token.expiration:604800}") // Default: 7 days in seconds
    private long refreshTokenExpiration;

    /**
     * Generate a new refresh token for a user
     * 
     * @param userId User ID
     * @param deviceInfo User agent or device identifier
     * @param ipAddress IP address
     * @return Generated refresh token entity
     */
    @Transactional
    public RefreshTokenEntity generateRefreshToken(Long userId, String deviceInfo, String ipAddress) {
        log.info("Generating refresh token for user: {}, device: {}, IP: {}", userId, deviceInfo, ipAddress);

        // Generate unique token using UUID
        String token = UUID.randomUUID().toString() + "_" + System.currentTimeMillis();
        
        // Calculate expiration (7 days from now)
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();

        RefreshTokenEntity saved = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token generated successfully: ID {}, expires at {}", saved.getId(), saved.getExpiresAt());
        
        return saved;
    }

    /**
     * Validate a refresh token
     * 
     * @param token Token string
     * @return RefreshTokenEntity if valid
     * @throws RuntimeException if token is invalid, expired, or revoked
     */
    public RefreshTokenEntity validateRefreshToken(String token) {
        log.debug("Validating refresh token: {}", token);

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found: {}", token);
                    return new RuntimeException("Invalid refresh token");
                });

        if (refreshToken.isRevoked()) {
            log.warn("Attempt to use revoked token: {}", token);
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            log.warn("Attempt to use expired token: {}", token);
            throw new RuntimeException("Refresh token has expired");
        }

        log.debug("Refresh token is valid: {}", token);
        return refreshToken;
    }

    /**
     * Rotate refresh token (generate new, revoke old)
     * Best practice for security: prevent token reuse
     * 
     * @param oldToken Old token string
     * @param deviceInfo Device info
     * @param ipAddress IP address
     * @return New refresh token entity
     */
    @Transactional
    public RefreshTokenEntity rotateRefreshToken(String oldToken, String deviceInfo, String ipAddress) {
        log.info("Rotating refresh token: {}", oldToken);

        // Validate old token
        RefreshTokenEntity oldRefreshToken = validateRefreshToken(oldToken);

        // Revoke old token
        oldRefreshToken.revoke();
        refreshTokenRepository.save(oldRefreshToken);
        log.debug("Old refresh token revoked: {}", oldToken);

        // Generate new token
        RefreshTokenEntity newRefreshToken = generateRefreshToken(
                oldRefreshToken.getUserId(), 
                deviceInfo, 
                ipAddress
        );

        log.info("Refresh token rotated successfully for user: {}", oldRefreshToken.getUserId());
        return newRefreshToken;
    }

    /**
     * Revoke a specific refresh token (logout single device)
     * 
     * @param token Token string
     */
    @Transactional
    public void revokeToken(String token) {
        log.info("Revoking refresh token: {}", token);

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (!refreshToken.isRevoked()) {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            log.info("Refresh token revoked successfully: {}", token);
        } else {
            log.debug("Refresh token already revoked: {}", token);
        }
    }

    /**
     * Revoke all refresh tokens for a user (logout all devices)
     * 
     * @param userId User ID
     * @return Number of tokens revoked
     */
    @Transactional
    public int revokeAllUserTokens(Long userId) {
        log.info("Revoking all refresh tokens for user: {}", userId);

        int count = refreshTokenRepository.revokeAllUserTokens(userId, LocalDateTime.now());
        log.info("Revoked {} refresh tokens for user: {}", count, userId);
        
        return count;
    }

    /**
     * Get all active sessions (refresh tokens) for a user
     * 
     * @param userId User ID
     * @return List of active refresh tokens
     */
    public List<RefreshTokenEntity> getActiveSessions(Long userId) {
        log.debug("Getting active sessions for user: {}", userId);
        return refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId);
    }

    /**
     * Revoke a specific session by ID
     * 
     * @param sessionId Refresh token ID
     * @param userId User ID (for authorization check)
     */
    @Transactional
    public void revokeSession(Long sessionId, Long userId) {
        log.info("Revoking session {} for user: {}", sessionId, userId);

        RefreshTokenEntity refreshToken = refreshTokenRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Authorization check
        if (!refreshToken.getUserId().equals(userId)) {
            log.warn("User {} attempted to revoke session {} belonging to user {}", 
                    userId, sessionId, refreshToken.getUserId());
            throw new RuntimeException("Unauthorized: Cannot revoke another user's session");
        }

        if (!refreshToken.isRevoked()) {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            log.info("Session revoked successfully: {}", sessionId);
        }
    }

    /**
     * Scheduled job to clean up expired tokens
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanExpiredTokens() {
        log.info("Running scheduled cleanup of expired refresh tokens");

        LocalDateTime now = LocalDateTime.now();
        int deletedCount = refreshTokenRepository.deleteByExpiresAtBefore(now);

        log.info("Cleaned up {} expired refresh tokens", deletedCount);
    }

    /**
     * Count active tokens for a user
     * 
     * @param userId User ID
     * @return Number of active tokens
     */
    public long countActiveTokens(Long userId) {
        return refreshTokenRepository.countActiveTokensByUserId(userId, LocalDateTime.now());
    }
}
