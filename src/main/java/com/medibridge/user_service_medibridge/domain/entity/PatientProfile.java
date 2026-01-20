package com.medibridge.user_service_medibridge.domain.entity;

import com.medibridge.user_service_medibridge.util.constant.BloodGroup;
import com.medibridge.user_service_medibridge.util.constant.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Patient profile with comprehensive healthcare information.
 * 
 * Security: Contains PII and PHI - handle with care
 * Compliance: HIPAA-ready, supports consent management
 * Audit: Full tracking of profile changes
 */
@Entity
@Table(name = "patient_profiles", indexes = {
        @Index(name = "idx_patient_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_patient_phone", columnList = "phone_number"),
        @Index(name = "idx_patient_government_id", columnList = "government_id_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "patient_profile_id", updatable = false, nullable = false)
    private UUID patientProfileId;

    // One-to-One relationship with User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Personal Information
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", length = 10)
    private BloodGroup bloodGroup;

    // Contact Information
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "alternate_phone_number", length = 20)
    private String alternatePhoneNumber;

    // Address Information
    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 100)
    @Builder.Default
    private String country = "India";

    // Emergency Contact
    @Column(name = "emergency_contact_name", nullable = false, length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_relationship", nullable = false, length = 50)
    private String emergencyContactRelationship;

    @Column(name = "emergency_contact_phone", nullable = false, length = 20)
    private String emergencyContactPhone;

    // Government ID (encrypted in production)
    @Column(name = "government_id_type", length = 50)
    private String governmentIdType; // Aadhar, PAN, Passport, etc.

    @Column(name = "government_id_number", length = 100)
    private String governmentIdNumber;

    // Insurance Information
    @Column(name = "insurance_provider", length = 100)
    private String insuranceProvider;

    @Column(name = "insurance_policy_number", length = 100)
    private String insurancePolicyNumber;

    @Column(name = "insurance_valid_until")
    private LocalDate insuranceValidUntil;

    // Medical Preferences
    @Column(name = "preferred_language", length = 50)
    @Builder.Default
    private String preferredLanguage = "English";

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "chronic_conditions", columnDefinition = "TEXT")
    private String chronicConditions;

    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;

    // Consent Management (HIPAA/GDPR compliance)
    @Column(name = "consent_for_treatment", nullable = false)
    @Builder.Default
    private Boolean consentForTreatment = false;

    @Column(name = "consent_for_treatment_date")
    private LocalDateTime consentForTreatmentDate;

    @Column(name = "consent_for_data_sharing", nullable = false)
    @Builder.Default
    private Boolean consentForDataSharing = false;

    @Column(name = "consent_for_data_sharing_date")
    private LocalDateTime consentForDataSharingDate;

    @Column(name = "consent_for_marketing", nullable = false)
    @Builder.Default
    private Boolean consentForMarketing = false;

    @Column(name = "consent_for_marketing_date")
    private LocalDateTime consentForMarketingDate;

    // Profile Status
    @Column(name = "profile_completed", nullable = false)
    @Builder.Default
    private Boolean profileCompleted = false;

    @Column(name = "profile_verified", nullable = false)
    @Builder.Default
    private Boolean profileVerified = false;

    @Column(name = "profile_verified_by", length = 100)
    private String profileVerifiedBy;

    @Column(name = "profile_verified_at")
    private LocalDateTime profileVerifiedAt;

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

    // Business methods
    // Business methods
    public boolean isConsentForTreatment() {
        return Boolean.TRUE.equals(consentForTreatment);
    }

    public boolean isConsentForDataSharing() {
        return Boolean.TRUE.equals(consentForDataSharing);
    }

    public boolean isConsentForMarketing() {
        return Boolean.TRUE.equals(consentForMarketing);
    }

    public boolean isProfileCompleted() {
        return Boolean.TRUE.equals(profileCompleted);
    }

    public boolean isProfileVerified() {
        return Boolean.TRUE.equals(profileVerified);
    }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ").append(middleName);
        }
        fullName.append(" ").append(lastName);
        return fullName.toString();
    }

    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public boolean hasValidInsurance() {
        return insuranceValidUntil != null && insuranceValidUntil.isAfter(LocalDate.now());
    }

    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
}
