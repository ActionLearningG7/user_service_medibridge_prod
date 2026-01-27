package com.medibridge.user_service_medibridge.api.controller;

import com.medibridge.user_service_medibridge.api.dto.request.CreateDoctorRequest;
import com.medibridge.user_service_medibridge.api.dto.request.VerifyDoctorRequest;
import com.medibridge.user_service_medibridge.api.dto.response.ApiResponse;
import com.medibridge.user_service_medibridge.api.dto.response.DoctorProfileResponse;
import com.medibridge.user_service_medibridge.domain.service.DoctorService;
import com.medibridge.user_service_medibridge.util.constant.Specialization;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Doctor Controller
 * 
 * Endpoints: /api/v1/doctors
 * Security: Mixed - some public, some ADMIN only
 */
@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor", description = "Doctor profile management endpoints")
public class DoctorController {

    private final DoctorService doctorService;

    /**
     * Create new doctor (ADMIN only)
     * 
     * @param request        Doctor creation data
     * @param authentication Current admin user
     * @return DoctorProfileResponse
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create doctor", description = "Create new doctor profile (ADMIN only)")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> createDoctor(
            @Valid @RequestBody CreateDoctorRequest request,
            Authentication authentication) {

        String createdBy = authentication.getName();
        DoctorProfileResponse response = doctorService.createDoctor(request, createdBy);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Doctor created successfully"));
    }

    /**
     * Get current doctor's profile
     * 
     * @param authentication Current doctor
     * @return DoctorProfileResponse
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get my profile", description = "Get current doctor's profile")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> getMyProfile(
            Authentication authentication) {

        String email = authentication.getName();
        DoctorProfileResponse response = doctorService.getDoctorProfileByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get doctor by ID (PUBLIC for verified doctors)
     * 
     * @param userId Doctor user ID
     * @return DoctorProfileResponse
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get doctor by ID", description = "Get doctor profile by user ID (PUBLIC)")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> getDoctorById(
            @PathVariable UUID userId) {

        DoctorProfileResponse response = doctorService.getDoctorProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all verified doctors (PUBLIC)
     * 
     * @return List of DoctorProfileResponse
     */
    @GetMapping
    @Operation(summary = "Get all verified doctors", description = "Get all verified and active doctors (PUBLIC)")
    public ResponseEntity<ApiResponse<List<DoctorProfileResponse>>> getVerifiedDoctors() {

        List<DoctorProfileResponse> response = doctorService.getVerifiedDoctors();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get doctors by specialization (PUBLIC)
     * 
     * @param specialization Medical specialization
     * @return List of DoctorProfileResponse
     */
    @GetMapping("/specialization/{specialization}")
    @Operation(summary = "Get doctors by specialization", description = "Get verified doctors by specialization (PUBLIC)")
    public ResponseEntity<ApiResponse<List<DoctorProfileResponse>>> getDoctorsBySpecialization(
            @PathVariable Specialization specialization) {

        List<DoctorProfileResponse> response = doctorService.getDoctorsBySpecialization(specialization);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get doctors by department (ADMIN only)
     * 
     * @param department Department name
     * @return List of DoctorProfileResponse
     */
    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get doctors by department", description = "Get doctors by department (ADMIN only)")
    public ResponseEntity<ApiResponse<List<DoctorProfileResponse>>> getDoctorsByDepartment(
            @PathVariable String department) {

        List<DoctorProfileResponse> response = doctorService.getDoctorsByDepartment(department);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get available doctors (PUBLIC)
     * 
     * @return List of DoctorProfileResponse
     */
    @GetMapping("/available")
    @Operation(summary = "Get available doctors", description = "Get doctors available for consultation (PUBLIC)")
    public ResponseEntity<ApiResponse<List<DoctorProfileResponse>>> getAvailableDoctors() {

        List<DoctorProfileResponse> response = doctorService.getAvailableDoctors();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get emergency doctors (PUBLIC)
     * 
     * @return List of DoctorProfileResponse
     */
    @GetMapping("/emergency")
    @Operation(summary = "Get emergency doctors", description = "Get doctors available for emergency (PUBLIC)")
    public ResponseEntity<ApiResponse<List<DoctorProfileResponse>>> getEmergencyDoctors() {

        List<DoctorProfileResponse> response = doctorService.getEmergencyDoctors();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get pending verifications (ADMIN only)
     * 
     * @return List of DoctorProfileResponse
     */
    @GetMapping("/pending-verification")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get pending verifications", description = "Get doctors pending verification (ADMIN only)")
    public ResponseEntity<ApiResponse<List<DoctorProfileResponse>>> getPendingVerifications() {

        List<DoctorProfileResponse> response = doctorService.getPendingVerifications();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Verify doctor (ADMIN only with permission)
     * 
     * @param doctorId       Doctor profile ID
     * @param request        Verification data
     * @param authentication Current admin
     * @return DoctorProfileResponse
     */
    @PostMapping("/{doctorId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Verify doctor", description = "Verify doctor credentials (ADMIN only)")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> verifyDoctor(
            @PathVariable UUID doctorId,
            @Valid @RequestBody VerifyDoctorRequest request,
            Authentication authentication) {

        String verifiedBy = authentication.getName();
        DoctorProfileResponse response = doctorService.verifyDoctor(doctorId, request, verifiedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Doctor verification updated successfully"));
    }

    /**
     * Search doctors (ADMIN only)
     * 
     * @param searchTerm Search by name or license
     * @return List of DoctorProfileResponse
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Search doctors", description = "Search doctors by name or license (ADMIN only)")
    public ResponseEntity<ApiResponse<List<DoctorProfileResponse>>> searchDoctors(
            @RequestParam String searchTerm) {

        List<DoctorProfileResponse> response = doctorService.searchDoctors(searchTerm);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get doctors with expiring licenses (ADMIN only)
     * 
     * @param days Days threshold
     * @return List of DoctorProfileResponse
     */
    @GetMapping("/expiring-licenses")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get expiring licenses", description = "Get doctors with licenses expiring soon (ADMIN only)")
    public ResponseEntity<ApiResponse<List<DoctorProfileResponse>>> getExpiringLicenses(
            @RequestParam(defaultValue = "30") int days) {

        List<DoctorProfileResponse> response = doctorService.getDoctorsWithExpiringLicenses(days);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Delete doctor profile (ADMIN only)
     * 
     * @param userId Doctor user ID
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete doctor", description = "Soft delete doctor profile (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable UUID userId) {

        doctorService.deleteDoctorProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Doctor profile deleted successfully"));
    }
}
