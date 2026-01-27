package com.medibridge.user_service_medibridge.domain.service;

import com.medibridge.user_service_medibridge.api.dto.response.AdminProfileResponse;
import com.medibridge.user_service_medibridge.util.constant.AdminLevel;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for admin operations.
 * 
 * Security: Highest privilege operations
 * Only SUPER_ADMIN can create other admins
 */
public interface AdminService {

    /**
     * Get admin profile by user ID
     */
    AdminProfileResponse getAdminProfile(UUID userId);

    /**
     * Get admin profile by email
     */
    AdminProfileResponse getAdminProfileByEmail(String email);

    /**
     * Get all admins (SUPER_ADMIN only)
     */
    List<AdminProfileResponse> getAllAdmins();

    /**
     * Get admins by level (SUPER_ADMIN only)
     */
    List<AdminProfileResponse> getAdminsByLevel(AdminLevel level);

    /**
     * Get admins who can verify doctors
     */
    List<AdminProfileResponse> getAdminsWhoCanVerifyDoctors();

    /**
     * Get non-compliant admins (missing training/acknowledgements)
     */
    List<AdminProfileResponse> getNonCompliantAdmins();

    /**
     * Update user status (ADMIN only)
     */
    void updateUserStatus(UUID userId, UserStatus status);

    /**
     * Suspend admin (SUPER_ADMIN only)
     */
    void suspendAdmin(UUID adminId, String reason, String suspendedBy);

    /**
     * Unsuspend admin (SUPER_ADMIN only)
     */
    void unsuspendAdmin(UUID adminId);

    /**
     * Get system statistics (ADMIN only)
     */
    Map<String, Object> getSystemStatistics();

    /**
     * Delete admin profile (soft delete - SUPER_ADMIN only)
     */
    void deleteAdminProfile(UUID userId);
}
