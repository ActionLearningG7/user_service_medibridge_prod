package com.medibridge.user_service_medibridge.domain.repository;

import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.util.constant.Role;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity operations.
 * 
 * Security: Provides methods for authentication and authorization
 * Audit: Supports querying by audit fields
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Authentication queries
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailOrUsername(String email, String username);

    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.username = :identifier) AND u.deleted = false")
    Optional<User> findByEmailOrUsernameAndNotDeleted(@Param("identifier") String identifier);

    // Existence checks
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByUsernameAndDeletedFalse(String username);

    boolean existsByPhoneNumberAndDeletedFalse(String phoneNumber);

    // Phone number queries
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmailAndDeletedFalse(@Param("email") String email);

    // Role-based queries
    List<User> findByRole(Role role);

    List<User> findByRoleAndDeletedFalse(Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status AND u.deleted = false")
    List<User> findByRoleAndStatus(@Param("role") Role role, @Param("status") UserStatus status);

    // Status queries
    List<User> findByStatus(UserStatus status);

    List<User> findByStatusAndDeletedFalse(UserStatus status);

    // Verification queries
    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token AND u.emailVerificationTokenExpiresAt > :now AND u.deleted = false")
    Optional<User> findByValidEmailVerificationToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetTokenExpiresAt > :now AND u.deleted = false")
    Optional<User> findByValidPasswordResetToken(@Param("token") String token, @Param("now") LocalDateTime now);

    // Security queries
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.deleted = false AND u.status = 'ACTIVE'")
    Optional<User> findActiveUserById(@Param("userId") UUID userId);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false AND u.status = 'ACTIVE'")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    // Account lockout queries
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > :now AND u.deleted = false")
    List<User> findLockedAccounts(@Param("now") LocalDateTime now);

    // Audit queries
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<User> findByCreatedBy(String createdBy);

    @Query("SELECT u FROM User u WHERE u.deleted = true")
    List<User> findDeletedUsers();

    @Query("SELECT u FROM User u WHERE u.deleted = true AND u.deletedAt BETWEEN :start AND :end")
    List<User> findDeletedUsersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Statistics
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.deleted = false")
    long countByRole(@Param("role") Role role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status AND u.deleted = false")
    long countByStatus(@Param("status") UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true AND u.deleted = false")
    long countVerifiedUsers();

    // Username queries for credential generation
    @Query("SELECT u.username FROM User u WHERE u.deleted = false")
    List<String> findAllUsernames();
}
