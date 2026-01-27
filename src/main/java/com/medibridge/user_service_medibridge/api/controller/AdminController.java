package com.medibridge.user_service_medibridge.api.controller;

import com.medibridge.user_service_medibridge.api.dto.response.AdminProfileResponse;
import com.medibridge.user_service_medibridge.api.dto.response.ApiResponse;
import com.medibridge.user_service_medibridge.api.dto.response.DoctorProfileResponse;
import com.medibridge.user_service_medibridge.domain.service.AdminService;
import com.medibridge.user_service_medibridge.domain.service.DoctorService;
import com.medibridge.user_service_medibridge.util.constant.AdminLevel;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin Controller
 * 
 * Endpoints: /api/v1/admin
 * Security: ADMIN only - highest privilege operations
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin", description = "Administrative operations and system management")
public class AdminController {

    private final AdminService adminService;
    private final DoctorService doctorService;

    /**
     * Get current admin's profile
     * 
     * @param authentication Current admin
     * @return AdminProfileResponse
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get my admin profile", description = "Get current admin's profile")
    public ResponseEntity<ApiResponse<AdminProfileResponse>> getMyProfile(
            Authentication authentication) {

        String email = authentication.getName();
        AdminProfileResponse response = adminService.getAdminProfileByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all admins (SUPER_ADMIN only)
     * 
     * @return List of AdminProfileResponse
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Additional check in service layer for SUPER_ADMIN
    @Operation(summary = "Get all admins", description = "Get all admin profiles (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<List<AdminProfileResponse>>> getAllAdmins() {

        List<AdminProfileResponse> response = adminService.getAllAdmins();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get admins by level (SUPER_ADMIN only)
     * 
     * @param level Admin level
     * @return List of AdminProfileResponse
     */
    @GetMapping("/level/{level}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admins by level", description = "Get admins by admin level (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<List<AdminProfileResponse>>> getAdminsByLevel(
            @PathVariable AdminLevel level) {

        List<AdminProfileResponse> response = adminService.getAdminsByLevel(level);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get admins who can verify doctors
     * 
     * @return List of AdminProfileResponse
     */
    @GetMapping("/verifiers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get doctor verifiers", description = "Get admins who can verify doctors")
    public ResponseEntity<ApiResponse<List<AdminProfileResponse>>> getDoctorVerifiers() {

        List<AdminProfileResponse> response = adminService.getAdminsWhoCanVerifyDoctors();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get non-compliant admins
     * 
     * @return List of AdminProfileResponse
     */
    @GetMapping("/non-compliant")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get non-compliant admins", description = "Get admins missing training/acknowledgements")
    public ResponseEntity<ApiResponse<List<AdminProfileResponse>>> getNonCompliantAdmins() {

        List<AdminProfileResponse> response = adminService.getNonCompliantAdmins();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update user status (ADMIN only)
     * 
     * @param userId User ID
     * @param status New status
     */
    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user status", description = "Update user account status (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam UserStatus status) {

        adminService.updateUserStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success(null, "User status updated successfully"));
    }

    /**
     * Suspend admin (SUPER_ADMIN only)
     * 
     * @param adminId        Admin user ID
     * @param reason         Suspension reason
     * @param authentication Current super admin
     */
    @PostMapping("/{adminId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspend admin", description = "Suspend admin account (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> suspendAdmin(
            @PathVariable UUID adminId,
            @RequestParam String reason,
            Authentication authentication) {

        String suspendedBy = authentication.getName();
        adminService.suspendAdmin(adminId, reason, suspendedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "Admin suspended successfully"));
    }

    /**
     * Unsuspend admin (SUPER_ADMIN only)
     * 
     * @param adminId Admin user ID
     */
    @PostMapping("/{adminId}/unsuspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unsuspend admin", description = "Unsuspend admin account (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> unsuspendAdmin(@PathVariable UUID adminId) {

        adminService.unsuspendAdmin(adminId);
        return ResponseEntity.ok(ApiResponse.success(null, "Admin unsuspended successfully"));
    }

    /**
     * Get system statistics (ADMIN only)
     * 
     * @return Map of statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get system statistics", description = "Get system-wide statistics (ADMIN only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStatistics() {

        Map<String, Object> response = adminService.getSystemStatistics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all doctors (ADMIN only)
     * 
     * @return List of all doctors
     */
    @GetMapping("/doctors")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all doctors", description = "Get all doctors for admin management (ADMIN only)")
    public ResponseEntity<ApiResponse<List<DoctorProfileResponse>>> getAllDoctors() {

        List<DoctorProfileResponse> response = doctorService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    /**
     * Delete admin profile (SUPER_ADMIN only)
     * 
     * @param userId Admin user ID
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete admin", description = "Soft delete admin profile (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable UUID userId) {

        adminService.deleteAdminProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Admin profile deleted successfully"));
    }
}
