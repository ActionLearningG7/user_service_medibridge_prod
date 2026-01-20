package com.medibridge.user_service_medibridge.domain.entity;

import com.medibridge.user_service_medibridge.util.constant.Role;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core User entity for authentication and authorization.
 * Represents the base user account in the hospital management system.
 * 
 * Security: Contains authentication credentials and role information.
 * Audit: Tracks creation, updates, and soft deletion.
 * Compliance: Supports account lockout and verification workflows.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_username", columnList = "username", unique = true),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_role", columnList = "role")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements org.springframework.data.domain.Persistable<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING_VERIFICATION;

    // Security fields
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private Boolean phoneVerified = false;

    @Column(name = "two_factor_enabled", nullable = false)
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret", length = 32)
    private String twoFactorSecret;

    // Account security
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;

    // Verification tokens
    @Column(name = "email_verification_token", length = 64)
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expires_at")
    private LocalDateTime emailVerificationTokenExpiresAt;

    @Column(name = "password_reset_token", length = 64)
    private String passwordResetToken;

    @Column(name = "password_reset_token_expires_at")
    private LocalDateTime passwordResetTokenExpiresAt;

    // Temporary password management (for doctor onboarding)
    @Column(name = "password_is_temporary", nullable = false)
    @Builder.Default
    private Boolean passwordIsTemporary = false;

    @Column(name = "password_must_change", nullable = false)
    @Builder.Default
    private Boolean passwordMustChange = false;

    @Column(name = "temporary_password_expires_at")
    private LocalDateTime temporaryPasswordExpiresAt;

    @Column(name = "credential_generated_at")
    private LocalDateTime credentialGeneratedAt;

    @Column(name = "credential_generated_by", length = 100)
    private String credentialGeneratedBy;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Soft delete
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    // Terms and conditions
    @Column(name = "terms_accepted", nullable = false)
    @Builder.Default
    private Boolean termsAccepted = false;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    @Column(name = "privacy_policy_accepted", nullable = false)
    @Builder.Default
    private Boolean privacyPolicyAccepted = false;

    @Column(name = "privacy_policy_accepted_at")
    private LocalDateTime privacyPolicyAcceptedAt;

    // Helper methods for Boolean fields
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public boolean isPhoneVerified() {
        return Boolean.TRUE.equals(phoneVerified);
    }

    public boolean isTwoFactorEnabled() {
        return Boolean.TRUE.equals(twoFactorEnabled);
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    // Business methods
    public boolean isAccountLocked() {
        return accountLockedUntil != null && accountLockedUntil.isAfter(LocalDateTime.now());
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE && !deleted && !isAccountLocked();
    }

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountLockedUntil = LocalDateTime.now().plusHours(1);
        }
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.status = UserStatus.DELETED;
    }

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return isNew || userId == null;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
