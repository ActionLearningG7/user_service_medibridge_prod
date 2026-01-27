package com.medibridge.user_service_medibridge.api.dto.response;

import com.medibridge.user_service_medibridge.util.constant.Gender;
import com.medibridge.user_service_medibridge.util.constant.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhlebotomistProfileResponse {

    private UUID phlebotomistProfileId;
    private UUID userId;

    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phoneNumber;

    private String certificationNumber;
    private String certificationIssuingBody;
    private LocalDate certificationExpiryDate;

    private String shiftType;
    private String assignedZone;
    private String employeeId;
    private LocalDate joiningDate;
    private String specialization;

    private VerificationStatus verificationStatus;
    private Boolean active;
    private String status;  // ACTIVE or INACTIVE
    private Boolean isAdmin;  // Phlebotomist admin privileges

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
