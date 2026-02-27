package com.imes.infra.repository;

import com.imes.infra.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for Password Reset Token operations
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    /**
     * Find password reset token by token string
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /**
     * Find valid (not expired, not used) token for user
     */
    @Query("SELECT t FROM PasswordResetTokenEntity t " +
           "WHERE t.userId = :userId " +
           "AND t.expiresAt > :now " +
           "AND t.usedAt IS NULL " +
           "ORDER BY t.createdAt DESC")
    Optional<PasswordResetTokenEntity> findValidTokenByUserId(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    /**
     * Delete all expired tokens (cleanup)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete all tokens for a specific user (e.g., after successful password reset)
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.userId = :userId")
    int deleteAllUserTokens(@Param("userId") Long userId);

    /**
     * Check if valid token exists for user
     */
    @Query("SELECT COUNT(t) > 0 FROM PasswordResetTokenEntity t " +
           "WHERE t.userId = :userId " +
           "AND t.expiresAt > :now " +
           "AND t.usedAt IS NULL")
    boolean hasValidToken(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );
}
