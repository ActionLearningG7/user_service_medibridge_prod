package com.medibridge.user_service_medibridge.api.dto.response;

import com.medibridge.user_service_medibridge.util.constant.BloodGroup;
import com.medibridge.user_service_medibridge.util.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for PatientProfile entity.
 * 
 * Security: Contains PII/PHI - use with caution
 * Compliance: Excludes highly sensitive fields (government ID)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileResponse {

    private UUID patientProfileId;
    private UUID userId;

    // Personal information
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private Integer age;
    private Gender gender;
    private BloodGroup bloodGroup;

    // Contact information
    private String phoneNumber;
    private String alternatePhoneNumber;

    // Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Emergency contact
    private String emergencyContactName;
    private String emergencyContactRelationship;
    private String emergencyContactPhone;

    // Insurance (masked)
    private String insuranceProvider;
    private String insurancePolicyNumber;
    private Boolean hasValidInsurance;
    private LocalDate insuranceValidUntil;

    // Medical preferences
    private String preferredLanguage;
    private String allergies;
    private String chronicConditions;
    private String currentMedications;

    // Consent status
    private Boolean consentForTreatment;
    private Boolean consentForDataSharing;
    private Boolean consentForMarketing;

    // Profile status
    private Boolean profileCompleted;
    private Boolean profileVerified;

    // Government ID
    private String governmentIdType;
    private String governmentIdNumber;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
