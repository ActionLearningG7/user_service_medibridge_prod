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
public class CreatePhlebotomistRequest {

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

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    // Credentials
    @NotBlank(message = "Certification number is required")
    private String certificationNumber;

    @NotBlank(message = "Certification issuing body is required")
    private String certificationIssuingBody;

    @NotNull(message = "Certification expiry date is required")
    @Future(message = "Certification expiry must be in future")
    private LocalDate certificationExpiryDate;

    // Employment
    @NotBlank(message = "Shift type is required")
    private String shiftType; // MORNING, EVENING...

    private String assignedZone;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private String employeeId;

    // Admin privileges - allows result upload and admin operations
    @Builder.Default
    private Boolean isAdmin = false;
}
