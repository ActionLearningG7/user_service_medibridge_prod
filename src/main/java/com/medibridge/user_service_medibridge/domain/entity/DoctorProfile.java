package com.medibridge.user_service_medibridge.domain.entity;

import com.medibridge.user_service_medibridge.util.constant.Gender;
import com.medibridge.user_service_medibridge.util.constant.Specialization;
import com.medibridge.user_service_medibridge.util.constant.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Doctor profile with medical credentials and verification.
 * 
 * Security: Contains professional credentials - verification required
 * Compliance: Medical license tracking and renewal workflows
 * Audit: Full tracking of credential changes and verifications
 */
@Entity
@Table(name = "doctor_profiles", indexes = {
        @Index(name = "idx_doctor_user_id", columnList = "user_id", unique = true),
        @Index(name = "idx_doctor_license", columnList = "medical_license_number", unique = true),
        @Index(name = "idx_doctor_specialization", columnList = "specialization"),
        @Index(name = "idx_doctor_verification", columnList = "verification_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "doctor_profile_id", updatable = false, nullable = false)
    private UUID doctorProfileId;

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

    // Contact Information
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "alternate_phone_number", length = 20)
    private String alternatePhoneNumber;

    // Professional Credentials
    @Column(name = "medical_license_number", nullable = false, unique = true, length = 50)
    private String medicalLicenseNumber;

    @Column(name = "medical_license_issuing_authority", nullable = false, length = 100)
    private String medicalLicenseIssuingAuthority;

    @Column(name = "medical_license_issue_date", nullable = false)
    private LocalDate medicalLicenseIssueDate;

    @Column(name = "medical_license_expiry_date", nullable = false)
    private LocalDate medicalLicenseExpiryDate;

    @Column(name = "medical_registration_number", length = 50)
    private String medicalRegistrationNumber;

    @Column(name = "medical_council", length = 100)
    private String medicalCouncil; // e.g., Medical Council of India

    // Education
    @Column(name = "medical_degree", nullable = false, length = 100)
    private String medicalDegree; // MBBS, MD, etc.

    @Column(name = "medical_school", nullable = false, length = 200)
    private String medicalSchool;

    @Column(name = "graduation_year", nullable = false)
    private Integer graduationYear;

    @Column(name = "additional_qualifications", columnDefinition = "TEXT")
    private String additionalQualifications;

    // Specialization
    @Enumerated(EnumType.STRING)
    @Column(name = "specialization", nullable = false, length = 50)
    private Specialization specialization;

    @Column(name = "sub_specialization", length = 100)
    private String subSpecialization;

    @Column(name = "years_of_experience", nullable = false)
    private Integer yearsOfExperience;

    // Employment
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation; // Consultant, Senior Consultant, etc.

    @Column(name = "employment_type", nullable = false, length = 50)
    @Builder.Default
    private String employmentType = "FULL_TIME"; // FULL_TIME, PART_TIME, VISITING

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    // Consultation
    @Column(name = "consultation_fee")
    private Double consultationFee;

    @Column(name = "available_for_consultation", nullable = false)
    @Builder.Default
    private Boolean availableForConsultation = true;

    @Column(name = "available_for_emergency", nullable = false)
    @Builder.Default
    private Boolean availableForEmergency = false;

    // Verification Status
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // Document Upload References (stored in file storage service)
    @Column(name = "license_document_url", length = 500)
    private String licenseDocumentUrl;

    @Column(name = "degree_certificate_url", length = 500)
    private String degreeCertificateUrl;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "signature_url", length = 500)
    private String signatureUrl;

    // Professional Details
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "languages_spoken", length = 255)
    private String languagesSpoken;

    @Column(name = "awards_and_recognition", columnDefinition = "TEXT")
    private String awardsAndRecognition;

    // Profile Status
    @Column(name = "profile_completed", nullable = false)
    @Builder.Default
    private Boolean profileCompleted = false;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = false; // Activated only after verification

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy; // Must be ADMIN

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
        StringBuilder fullName = new StringBuilder("Dr. ").append(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ").append(middleName);
        }
        fullName.append(" ").append(lastName);
        return fullName.toString();
    }

    public boolean isLicenseValid() {
        return medicalLicenseExpiryDate != null && medicalLicenseExpiryDate.isAfter(LocalDate.now());
    }

    public boolean isVerified() {
        return verificationStatus == VerificationStatus.VERIFIED && active;
    }

    public void approve(String approvedBy) {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verifiedBy = approvedBy;
        this.verifiedAt = LocalDateTime.now();
        this.active = true;
    }

    public void reject(String rejectedBy, String reason) {
        this.verificationStatus = VerificationStatus.REJECTED;
        this.verifiedBy = rejectedBy;
        this.verifiedAt = LocalDateTime.now();
        this.rejectionReason = reason;
        this.active = false;
    }

    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.active = false;
    }
}
