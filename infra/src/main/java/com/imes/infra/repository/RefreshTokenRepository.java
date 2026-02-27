package com.imes.infra.repository;

import com.imes.infra.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RefreshToken operations
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    /**
     * Find refresh token by token string
     */
    Optional<RefreshTokenEntity> findByToken(String token);

    /**
     * Find all active (non-revoked) refresh tokens for a user
     */
    @Query("SELECT rt FROM RefreshTokenEntity rt WHERE rt.userId = :userId AND rt.revokedAt IS NULL")
    List<RefreshTokenEntity> findByUserIdAndRevokedAtIsNull(@Param("userId") Long userId);

    /**
     * Find all refresh tokens for a user (including revoked)
     */
    List<RefreshTokenEntity> findByUserId(Long userId);

    /**
     * Delete expired tokens (for cleanup job)
     */
    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.expiresAt < :date")
    int deleteByExpiresAtBefore(@Param("date") LocalDateTime date);

    /**
     * Revoke all active tokens for a user (logout all devices)
     */
    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revokedAt = :revokedAt WHERE rt.userId = :userId AND rt.revokedAt IS NULL")
    int revokeAllUserTokens(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Count active tokens for a user
     */
    @Query("SELECT COUNT(rt) FROM RefreshTokenEntity rt WHERE rt.userId = :userId AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    long countActiveTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Check if token exists and is valid
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END FROM RefreshTokenEntity rt WHERE rt.token = :token AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    boolean existsByTokenAndValid(@Param("token") String token, @Param("now") LocalDateTime now);
}
