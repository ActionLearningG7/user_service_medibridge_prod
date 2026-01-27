package com.medibridge.user_service_medibridge.domain.entity;

import com.medibridge.user_service_medibridge.util.constant.Gender;
import com.medibridge.user_service_medibridge.util.constant.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Phlebotomist profile with certification and shift details.
 */
@Entity
@Table(name = "phlebotomist_profiles", indexes = {
        @Index(name = "idx_phlebotomist_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_certification_number", columnList = "certification_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhlebotomistProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "phlebotomist_profile_id", updatable = false, nullable = false)
    private UUID phlebotomistProfileId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Personal Information
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    // Credentials
    @Column(name = "certification_number", nullable = false, unique = true, length = 50)
    private String certificationNumber;

    @Column(name = "certification_issuing_body", nullable = false, length = 100)
    private String certificationIssuingBody;

    @Column(name = "certification_expiry_date", nullable = false)
    private LocalDate certificationExpiryDate;

    // Employment
    @Column(name = "shift_type", nullable = false, length = 20)
    private String shiftType; // MORNING, EVENING, NIGHT, ROTATIONAL

    @Column(name = "assigned_zone", length = 100)
    private String assignedZone;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(name = "specialization", length = 100)
    private String specialization;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = false;

    // Admin privileges - allows result upload and admin operations
    @Column(name = "is_admin", nullable = false)
    @Builder.Default
    private Boolean isAdmin = false;

    // Audit
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    // Soft Delete
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.active = false;
    }
}
