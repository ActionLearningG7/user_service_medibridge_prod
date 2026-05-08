package com.medibridge.user_service_medibridge.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for ambulance driver profile.
 * Used for listing, viewing, and updating driver information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmbulanceDriverProfileResponse {

    private UUID profileId;
    private UUID userId;
    private UUID organizationId;

    // Driver Information
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;

    // License
    private String licenseNumber;
    private LocalDateTime licenseExpiry;

    // Vehicle
    private String vehicleRegistrationNumber;
    private UUID ambulanceId;

    // Service Area
    private String serviceArea;
    private Double homeBaseLatitude;
    private Double homeBaseLongitude;

    // Status
    private Boolean isActive;
    private String onDutyStatus;

    // Employment
    private String employeeId;
    private Integer yearsOfExperience;
    private String certificationNumber;

    // Metrics
    private Integer totalShiftsCompleted;
    private Integer totalIncidentsHandled;
    private Double averageRating;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
