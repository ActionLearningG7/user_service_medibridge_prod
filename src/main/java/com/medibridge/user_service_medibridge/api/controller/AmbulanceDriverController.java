package com.medibridge.user_service_medibridge.api.controller;

import com.medibridge.user_service_medibridge.api.dto.request.CreateAmbulanceDriverRequest;
import com.medibridge.user_service_medibridge.api.dto.response.ApiResponse;
import com.medibridge.user_service_medibridge.api.dto.response.AmbulanceDriverOnboardingResponse;
import com.medibridge.user_service_medibridge.api.dto.response.AmbulanceDriverProfileResponse;
import com.medibridge.user_service_medibridge.domain.service.AmbulanceDriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Ambulance Driver Controller
 *
 * Endpoints: /api/v1/admin/ambulance-drivers
 * Security: ADMIN only - ambulance driver management
 */
@RestController
@RequestMapping("/api/v1/admin/ambulance-drivers")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Ambulance Driver", description = "Ambulance driver profile management endpoints")
public class AmbulanceDriverController {

    private final AmbulanceDriverService driverService;

    /**
     * Create new ambulance driver (ADMIN only)
     *
     * @param request        Driver creation data
     * @param authentication Current admin user
     * @return AmbulanceDriverOnboardingResponse with credentials
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create ambulance driver", description = "Create new ambulance driver with user account (ADMIN only)")
    public ResponseEntity<ApiResponse<AmbulanceDriverOnboardingResponse>> createAmbulanceDriver(
            @Valid @RequestBody CreateAmbulanceDriverRequest request,
            Authentication authentication) {

        String createdBy = authentication.getName();
        // TODO: Get organization ID from authenticated admin or request
        UUID organizationId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Placeholder

        AmbulanceDriverOnboardingResponse response = driverService.createAmbulanceDriver(
                request,
                organizationId,
                createdBy);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Ambulance driver created successfully. Credentials displayed once only."));
    }

    /**
     * Get all ambulance drivers (ADMIN only)
     *
     * @return List of AmbulanceDriverProfileResponse
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all ambulance drivers", description = "Get all ambulance drivers (ADMIN only)")
    public ResponseEntity<ApiResponse<Page<AmbulanceDriverProfileResponse>>> getAllDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String onDutyStatus) {

        Pageable pageable = PageRequest.of(page, size);
        Page<AmbulanceDriverProfileResponse> response = driverService.getAllDriversPageable(
                pageable,
                search,
                status,
                onDutyStatus);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get ambulance driver by ID (ADMIN only)
     *
     * @param userId Driver user ID
     * @return AmbulanceDriverProfileResponse
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get driver by ID", description = "Get ambulance driver profile by user ID (ADMIN only)")
    public ResponseEntity<ApiResponse<AmbulanceDriverProfileResponse>> getDriver(
            @PathVariable UUID userId) {

        AmbulanceDriverProfileResponse response = driverService.getAmbulanceDriver(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update ambulance driver status (ADMIN only)
     *
     * @param userId Driver user ID
     * @param status ACTIVE or INACTIVE
     * @return Updated AmbulanceDriverProfileResponse
     */
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update driver status", description = "Update ambulance driver status (ACTIVE/INACTIVE)")
    public ResponseEntity<ApiResponse<AmbulanceDriverProfileResponse>> updateDriverStatus(
            @PathVariable UUID userId,
            @RequestParam String status,
            Authentication authentication) {

        String updatedBy = authentication.getName();
        AmbulanceDriverProfileResponse response = driverService.updateDriverStatus(userId, status, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Driver status updated successfully"));
    }

    /**
     * Pause ambulance driver from duty (ADMIN only)
     *
     * @param userId Driver user ID
     * @param reason Reason for pause
     * @return Updated AmbulanceDriverProfileResponse
     */
    @PatchMapping("/{userId}/pause")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Pause driver", description = "Pause driver from duty")
    public ResponseEntity<ApiResponse<AmbulanceDriverProfileResponse>> pauseDriver(
            @PathVariable UUID userId,
            @RequestParam String reason,
            Authentication authentication) {

        String updatedBy = authentication.getName();
        AmbulanceDriverProfileResponse response = driverService.pauseDriver(userId, reason, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Driver paused successfully"));
    }

    /**
     * Resume ambulance driver to duty (ADMIN only)
     *
     * @param userId Driver user ID
     * @return Updated AmbulanceDriverProfileResponse
     */
    @PatchMapping("/{userId}/resume")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resume driver", description = "Resume driver to duty")
    public ResponseEntity<ApiResponse<AmbulanceDriverProfileResponse>> resumeDriver(
            @PathVariable UUID userId,
            Authentication authentication) {

        String updatedBy = authentication.getName();
        AmbulanceDriverProfileResponse response = driverService.resumeDriver(userId, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Driver resumed successfully"));
    }

    /**
     * Reset driver credentials (ADMIN only)
     * Generate new temporary password and send via email
     *
     * @param userId Driver user ID
     * @return AmbulanceDriverOnboardingResponse with new credentials
     */
    @PostMapping("/{userId}/reset-credentials")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset driver credentials", description = "Reset driver credentials and send new temporary password")
    public ResponseEntity<ApiResponse<AmbulanceDriverOnboardingResponse>> resetCredentials(
            @PathVariable UUID userId,
            Authentication authentication) {

        String resetBy = authentication.getName();
        AmbulanceDriverOnboardingResponse response = driverService.resetDriverCredentials(userId, resetBy);
        return ResponseEntity.ok(ApiResponse.success(response, "New credentials generated and sent to driver"));
    }

    /**
     * Delete ambulance driver (ADMIN only - soft delete)
     *
     * @param userId Driver user ID
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete driver", description = "Soft delete driver profile (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(
            @PathVariable UUID userId,
            Authentication authentication) {

        String deletedBy = authentication.getName();
        driverService.deleteDriver(userId, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "Driver profile deleted successfully"));
    }
}
