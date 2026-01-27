package com.medibridge.user_service_medibridge.api.dto.request;

import com.medibridge.user_service_medibridge.util.constant.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for updating phlebotomist information
 * All fields are optional to support partial updates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePhlebotomistRequest {

    @Email(message = "Email must be valid")
    private String email;

    private String firstName;

    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private Gender gender;

    private String phoneNumber;

    // Credentials
    private String certificationNumber;

    private String certificationIssuingBody;

    @Future(message = "Certification expiry must be in future")
    private LocalDate certificationExpiryDate;

    // Employment
    private String shiftType;

    private String assignedZone;

    private LocalDate joiningDate;

    private String employeeId;

    private String specialization;

    // Admin privileges
    private Boolean isAdmin;
}
