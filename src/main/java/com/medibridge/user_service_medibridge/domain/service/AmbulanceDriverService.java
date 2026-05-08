package com.medibridge.user_service_medibridge.domain.service;

import com.medibridge.user_service_medibridge.api.dto.request.CreateAmbulanceDriverRequest;
import com.medibridge.user_service_medibridge.api.dto.response.AmbulanceDriverOnboardingResponse;
import com.medibridge.user_service_medibridge.api.dto.response.AmbulanceDriverProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Ambulance Driver profile management
 */
public interface AmbulanceDriverService {

    /**
     * Create new ambulance driver with user account (ADMIN only)
     * Returns credentials that should be displayed to admin once
     */
    AmbulanceDriverOnboardingResponse createAmbulanceDriver(
            CreateAmbulanceDriverRequest request,
            UUID organizationId,
            String createdBy
    );

    /**
     * Get ambulance driver profile by email
     */
    AmbulanceDriverProfileResponse getAmbulanceDriverByEmail(String email);

    /**
     * Get ambulance driver profile by user ID
     */
    AmbulanceDriverProfileResponse getAmbulanceDriver(UUID userId);

    /**
     * Get all active ambulance drivers
     */
    List<AmbulanceDriverProfileResponse> getAllActiveDrivers();

    /**
     * Get all ambulance drivers (ADMIN only)
     */
    List<AmbulanceDriverProfileResponse> getAllDrivers();

    /**
     * Get all drivers with pagination and filtering
     */
    Page<AmbulanceDriverProfileResponse> getAllDriversPageable(
            Pageable pageable,
            String search,
            String status,
            String onDutyStatus
    );

    /**
     * Update driver information
     */
    AmbulanceDriverProfileResponse updateDriver(
            UUID userId,
            CreateAmbulanceDriverRequest request,
            String updatedBy
    );

    /**
     * Update driver status (ACTIVE/INACTIVE)
     */
    AmbulanceDriverProfileResponse updateDriverStatus(UUID userId, String status, String updatedBy);

    /**
     * Update driver on-duty status (OFF_DUTY, ON_DUTY, RESPONDING, BUSY)
     */
    AmbulanceDriverProfileResponse updateOnDutyStatus(UUID userId, String onDutyStatus, String updatedBy);

    /**
     * Suspend/Pause driver from duty
     */
    AmbulanceDriverProfileResponse pauseDriver(UUID userId, String reason, String updatedBy);

    /**
     * Resume driver duty
     */
    AmbulanceDriverProfileResponse resumeDriver(UUID userId, String updatedBy);

    /**
     * Reset driver credentials (generate new temporary password)
     */
    AmbulanceDriverOnboardingResponse resetDriverCredentials(UUID userId, String resetBy);

    /**
     * Delete driver profile (soft delete)
     */
    void deleteDriver(UUID userId, String deletedBy);
}
