package com.medibridge.user_service_medibridge.api.dto.request;

import com.medibridge.user_service_medibridge.util.constant.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAmbulanceDriverRequest {

    // Identity Information
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    // Contact Information
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phoneNumber;

    @Email(message = "Emergency contact email must be valid")
    private String emergencyContactEmail;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Emergency contact phone must be valid")
    private String emergencyContactPhone;

    @NotBlank(message = "Emergency contact name is required")
    private String emergencyContactName;

    // License Information
    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotNull(message = "License expiry date is required")
    @Future(message = "License expiry must be in future")
    private LocalDate licenseExpiry;

    // Employee Information
    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private String employeeId;

    @Min(value = 0, message = "Years of experience must be non-negative")
    private Integer yearsOfExperience;

    @NotBlank(message = "Vehicle registration number is required")
    private String vehicleRegistrationNumber;

    private String certificationNumber;

    // Service Area
    private String serviceArea;

    private Double homeBaseLatitude;

    private Double homeBaseLongitude;
}
