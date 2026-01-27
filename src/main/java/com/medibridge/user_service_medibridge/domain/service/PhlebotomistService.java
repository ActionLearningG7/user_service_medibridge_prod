package com.medibridge.user_service_medibridge.domain.service;

import com.medibridge.user_service_medibridge.api.dto.request.CreatePhlebotomistRequest;
import com.medibridge.user_service_medibridge.api.dto.request.UpdatePhlebotomistRequest;
import com.medibridge.user_service_medibridge.api.dto.response.PhlebotomistProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Phlebotomist profile management
 */
public interface PhlebotomistService {

    /**
     * Create new phlebotomist profile (ADMIN only)
     */
    PhlebotomistProfileResponse createPhlebotomist(CreatePhlebotomistRequest request, String createdBy);

    /**
     * Get phlebotomist profile by email
     */
    PhlebotomistProfileResponse getPhlebotomistProfileByEmail(String email);

    /**
     * Get phlebotomist profile by user ID
     */
    PhlebotomistProfileResponse getPhlebotomistProfile(UUID userId);

    /**
     * Get all active phlebotomists
     */
    List<PhlebotomistProfileResponse> getAllActivePhlebotomists();

    /**
     * Get all phlebotomists (ADMIN only)
     */
    List<PhlebotomistProfileResponse> getAllPhlebotomists();

    /**
     * Get all phlebotomists with pagination and filtering
     */
    Page<PhlebotomistProfileResponse> getAllPhlebotomistsPageable(Pageable pageable, String search, String status);

    /**
     * Update phlebotomist information
     */
    PhlebotomistProfileResponse updatePhlebotomist(UUID userId, UpdatePhlebotomistRequest request, String updatedBy);

    /**
     * Update phlebotomist status (ACTIVE/INACTIVE)
     */
    PhlebotomistProfileResponse updatePhlebotomistStatus(UUID userId, String status, String updatedBy);

    /**
     * Reset phlebotomist credentials
     */
    void resetPhlebotomistCredentials(UUID userId, String resetBy);

    /**
     * Delete phlebotomist profile (soft delete)
     */
    void deletePhlebotomistProfile(UUID userId, String deletedBy);
}
