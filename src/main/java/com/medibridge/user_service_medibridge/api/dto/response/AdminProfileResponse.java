package com.medibridge.user_service_medibridge.api.dto.response;

import com.medibridge.user_service_medibridge.util.constant.AdminLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for AdminProfile entity.
 * 
 * Security: Restricted to admin users only
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileResponse {

    private UUID adminProfileId;
    private UUID userId;

    // Personal information
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;

    // Admin privileges
    private AdminLevel adminLevel;
    private String department;
    private String designation;
    private String reportingTo;

    // Permissions
    private Boolean canCreateUsers;
    private Boolean canModifyUsers;
    private Boolean canDeleteUsers;
    private Boolean canCreateDoctors;
    private Boolean canVerifyDoctors;
    private Boolean canManageAppointments;
    private Boolean canViewReports;
    private Boolean canManageSystemConfig;

    // Compliance
    private Boolean hipaaTrainingCompleted;
    private LocalDateTime hipaaTrainingDate;
    private Boolean dataPrivacyAcknowledged;
    private Boolean codeOfConductAccepted;
    private Boolean fullyCompliant;
    private String complianceStatus;

    // Employment
    private String employeeId;
    private LocalDateTime joiningDate;

    // Status
    private Boolean active;
    private Boolean suspended;
    private String suspendedReason;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
