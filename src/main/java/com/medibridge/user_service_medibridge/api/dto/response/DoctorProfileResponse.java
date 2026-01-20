package com.medibridge.user_service_medibridge.api.dto.response;

import com.medibridge.user_service_medibridge.util.constant.Gender;
import com.medibridge.user_service_medibridge.util.constant.Specialization;
import com.medibridge.user_service_medibridge.util.constant.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for DoctorProfile entity.
 * 
 * Security: Public fields for verified doctors, full details for admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileResponse {

    private UUID doctorProfileId;
    private UUID userId;

    // Personal information
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phoneNumber;

    // Professional credentials
    private String medicalLicenseNumber;
    private String medicalLicenseIssuingAuthority;
    private LocalDate medicalLicenseExpiryDate;
    private Boolean licenseValid;
    private String medicalRegistrationNumber;
    private String medicalCouncil;

    // Education
    private String medicalDegree;
    private String medicalSchool;
    private Integer graduationYear;
    private String additionalQualifications;

    // Specialization
    private Specialization specialization;
    private String subSpecialization;
    private Integer yearsOfExperience;

    // Employment
    private String department;
    private String designation;
    private String employmentType;

    // Consultation
    private Double consultationFee;
    private Boolean availableForConsultation;
    private Boolean availableForEmergency;

    // Verification
    private VerificationStatus verificationStatus;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private String verificationNotes;
    private String rejectionReason;

    // Professional details
    private String bio;
    private String languagesSpoken;
    private String photoUrl;

    // Status
    private Boolean active;
    private Boolean profileCompleted;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
