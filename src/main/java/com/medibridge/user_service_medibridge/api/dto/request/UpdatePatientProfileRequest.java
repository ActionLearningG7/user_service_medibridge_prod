package com.medibridge.user_service_medibridge.api.dto.request;

import com.medibridge.user_service_medibridge.util.constant.BloodGroup;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating patient profile.
 * 
 * Security: Only the patient can update their own profile
 * Validation: All fields are optional (partial update)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePatientProfileRequest {

    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Middle name must not exceed 100 characters")
    private String middleName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    private BloodGroup bloodGroup;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phoneNumber;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Alternate phone number must be valid")
    private String alternatePhoneNumber;

    // Address
    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    // Emergency contact
    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;

    @Size(max = 50, message = "Emergency contact relationship must not exceed 50 characters")
    private String emergencyContactRelationship;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Emergency contact phone must be valid")
    private String emergencyContactPhone;

    // Government ID
    @Size(max = 50, message = "Government ID type must not exceed 50 characters")
    private String governmentIdType;

    @Size(max = 100, message = "Government ID number must not exceed 100 characters")
    private String governmentIdNumber;

    // Insurance
    @Size(max = 100, message = "Insurance provider must not exceed 100 characters")
    private String insuranceProvider;

    @Size(max = 100, message = "Insurance policy number must not exceed 100 characters")
    private String insurancePolicyNumber;

    private LocalDate insuranceValidUntil;

    // Medical preferences
    @Size(max = 50, message = "Preferred language must not exceed 50 characters")
    private String preferredLanguage;

    private String allergies;

    private String chronicConditions;

    private String currentMedications;

    // Consent
    private Boolean consentForTreatment;

    private Boolean consentForDataSharing;

    private Boolean consentForMarketing;
}
