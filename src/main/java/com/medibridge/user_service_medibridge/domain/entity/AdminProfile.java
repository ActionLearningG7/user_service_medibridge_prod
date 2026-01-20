package com.medibridge.user_service_medibridge.domain.entity;

import com.medibridge.user_service_medibridge.util.constant.AdminLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Admin profile with hierarchical permissions.
 * 
 * Security: Highest privilege level - strict access control
 * Compliance: Audit trail for all admin actions
 * Governance: Permission scope and compliance acknowledgements
 */
@Entity
@Table(name = "admin_profiles", indexes = {
        @Index(name = "idx_admin_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_admin_level", columnList = "admin_level"),
        @Index(name = "idx_admin_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "admin_profile_id", updatable = false, nullable = false)
    private UUID adminProfileId;

    // One-to-One relationship with User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Personal Information
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    // Admin Privileges
    @Enumerated(EnumType.STRING)
    @Column(name = "admin_level", nullable = false, length = 30)
    private AdminLevel adminLevel;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    // Permission Scope
    @Column(name = "can_create_users", nullable = false)
    @Builder.Default
    private Boolean canCreateUsers = true;

    @Column(name = "can_modify_users", nullable = false)
    @Builder.Default
    private Boolean canModifyUsers = true;

    @Column(name = "can_delete_users", nullable = false)
    @Builder.Default
    private Boolean canDeleteUsers = false; // Restricted by default

    @Column(name = "can_create_doctors", nullable = false)
    @Builder.Default
    private Boolean canCreateDoctors = true;

    @Column(name = "can_verify_doctors", nullable = false)
    @Builder.Default
    private Boolean canVerifyDoctors = false; // Only certain admins

    @Column(name = "can_manage_appointments", nullable = false)
    @Builder.Default
    private Boolean canManageAppointments = true;

    @Column(name = "can_view_reports", nullable = false)
    @Builder.Default
    private Boolean canViewReports = true;

    @Column(name = "can_manage_system_config", nullable = false)
    @Builder.Default
    private Boolean canManageSystemConfig = false; // Super admin only

    // Access Restrictions
    @Column(name = "ip_whitelist", columnDefinition = "TEXT")
    private String ipWhitelist; // Comma-separated IPs

    @Column(name = "access_hours_start", length = 5)
    private String accessHoursStart; // e.g., "09:00"

    @Column(name = "access_hours_end", length = 5)
    private String accessHoursEnd; // e.g., "18:00"

    @Column(name = "require_mfa", nullable = false)
    @Builder.Default
    private Boolean requireMfa = true; // Multi-factor authentication required

    // Compliance
    @Column(name = "hipaa_training_completed", nullable = false)
    @Builder.Default
    private Boolean hipaaTrainingCompleted = false;

    @Column(name = "hipaa_training_date")
    private LocalDateTime hipaaTrainingDate;

    @Column(name = "data_privacy_acknowledged", nullable = false)
    @Builder.Default
    private Boolean dataPrivacyAcknowledged = false;

    @Column(name = "data_privacy_acknowledged_date")
    private LocalDateTime dataPrivacyAcknowledgedDate;

    @Column(name = "code_of_conduct_accepted", nullable = false)
    @Builder.Default
    private Boolean codeOfConductAccepted = false;

    @Column(name = "code_of_conduct_accepted_date")
    private LocalDateTime codeOfConductAcceptedDate;

    // Employment
    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(name = "joining_date", nullable = false)
    private LocalDateTime joiningDate;

    @Column(name = "reporting_to", length = 100)
    private String reportingTo;

    // Status
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "suspended", nullable = false)
    @Builder.Default
    private Boolean suspended = false;

    @Column(name = "suspended_reason", columnDefinition = "TEXT")
    private String suspendedReason;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "suspended_by", length = 100)
    private String suspendedBy;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy; // Must be higher-level ADMIN

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

    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isFullyCompliant() {
        return hipaaTrainingCompleted &&
                dataPrivacyAcknowledged &&
                codeOfConductAccepted;
    }

    public boolean canPerformAction() {
        return active && !suspended && !deleted && isFullyCompliant();
    }

    public void suspend(String suspendedBy, String reason) {
        this.suspended = true;
        this.suspendedBy = suspendedBy;
        this.suspendedReason = reason;
        this.suspendedAt = LocalDateTime.now();
        this.active = false;
    }

    public void unsuspend() {
        this.suspended = false;
        this.suspendedBy = null;
        this.suspendedReason = null;
        this.suspendedAt = null;
        this.active = true;
    }

    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.active = false;
    }
}
