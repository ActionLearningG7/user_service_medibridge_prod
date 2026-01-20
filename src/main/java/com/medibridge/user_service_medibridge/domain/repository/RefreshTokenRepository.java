package com.medibridge.user_service_medibridge.domain.repository;

import com.medibridge.user_service_medibridge.domain.entity.RefreshToken;
import com.medibridge.user_service_medibridge.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RefreshToken entity operations.
 * 
 * Security: Manages JWT refresh tokens
 * Compliance: Supports token rotation and revocation
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // Token queries
    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // User queries
    List<RefreshToken> findByUser(User user);

    List<RefreshToken> findByUserAndRevokedFalse(User user);

    List<RefreshToken> findByUserUserId(UUID userId);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.userId = :userId AND rt.revoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    // Cleanup queries
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.userId = :userId")
    void deleteByUserId(@Param("userId") UUID userId);

    // Revocation
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :now WHERE rt.user.userId = :userId")
    void revokeAllUserTokens(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = :now WHERE rt.token = :token")
    void revokeToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // Statistics
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.userId = :userId AND rt.revoked = false")
    long countActiveTokensByUserId(@Param("userId") UUID userId);
}
