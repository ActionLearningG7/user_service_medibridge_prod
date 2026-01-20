package com.medibridge.user_service_medibridge.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for doctor onboarding process.
 * 
 * Security: Contains NO sensitive credentials
 * Purpose: Confirms successful creation and notification
 * Audit: Includes generation timestamps for compliance
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorOnboardingResponse {

    private UUID userId;
    private UUID doctorProfileId;
    private String username;
    private String email;
    private String fullName;
    private String specialization;

    // Onboarding status
    private boolean credentialsGenerated;
    private boolean notificationSent;
    private LocalDateTime credentialGeneratedAt;
    private LocalDateTime notificationSentAt;

    // Security status
    private boolean passwordIsTemporary;
    private boolean passwordMustChange;
    private LocalDateTime temporaryPasswordExpiresAt;

    // Audit
    private String createdBy;
    private LocalDateTime createdAt;

    // Important: Credentials are NEVER included in response
    // They are sent only via secure email notification
}
