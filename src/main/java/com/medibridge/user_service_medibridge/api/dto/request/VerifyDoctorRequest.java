package com.medibridge.user_service_medibridge.api.dto.request;

import com.medibridge.user_service_medibridge.util.constant.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying doctor credentials.
 * 
 * Security: Only ADMIN with canVerifyDoctors permission
 * Compliance: Updates verification status and notes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyDoctorRequest {

    @NotNull(message = "Verification status is required")
    private VerificationStatus verificationStatus;

    @Size(max = 1000, message = "Verification notes must not exceed 1000 characters")
    private String verificationNotes;

    @Size(max = 500, message = "Rejection reason must not exceed 500 characters")
    private String rejectionReason;
}
