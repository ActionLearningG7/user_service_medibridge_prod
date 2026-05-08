package com.medibridge.user_service_medibridge.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for ambulance driver creation.
 *
 * Security: Contains generated credentials that should be displayed to admin only once
 * Purpose: Confirms successful driver creation and provides login information
 * Audit: Includes generation timestamps for compliance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmbulanceDriverOnboardingResponse {

    // User Information
    private UUID userId;
    private UUID profileId;
    private String username;
    private String email;
    private String fullName;

    // Credentials (shown once, then user must change)
    private String generatedPassword;
    private boolean passwordIsTemporary;
    private boolean passwordMustChange;
    private LocalDateTime temporaryPasswordExpiresAt;

    // Driver Details
    private String licenseNumber;
    private String vehicleRegistrationNumber;
    private String employeeId;

    // Status
    private boolean credentialsGenerated;
    private LocalDateTime credentialGeneratedAt;
}
