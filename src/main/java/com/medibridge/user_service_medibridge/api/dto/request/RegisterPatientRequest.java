package com.medibridge.user_service_medibridge.api.dto.request;

import com.medibridge.user_service_medibridge.util.constant.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for patient self-registration.
 * 
 * Security: Only PATIENT role can self-register
 * Validation: All required fields validated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPatientRequest {

    // User credentials
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$", message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character")
    private String password;

    // Personal information
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Middle name must not exceed 100 characters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    // Contact information
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phoneNumber;

    // Address information
    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    private String country;

    // Emergency contact
    @NotBlank(message = "Emergency contact name is required")
    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact relationship is required")
    @Size(max = 50, message = "Emergency contact relationship must not exceed 50 characters")
    private String emergencyContactRelationship;

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Emergency contact phone must be valid")
    private String emergencyContactPhone;

    // Terms acceptance
    @NotNull(message = "Terms acceptance is required")
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean termsAccepted;

    @NotNull(message = "Privacy policy acceptance is required")
    @AssertTrue(message = "You must accept the privacy policy")
    private Boolean privacyPolicyAccepted;
}
