package com.medibridge.user_service_medibridge.api.controller;

import com.medibridge.user_service_medibridge.api.dto.request.CreatePhlebotomistRequest;
import com.medibridge.user_service_medibridge.api.dto.response.ApiResponse;
import com.medibridge.user_service_medibridge.api.dto.response.PhlebotomistProfileResponse;
import com.medibridge.user_service_medibridge.domain.service.PhlebotomistService;
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
 * Phlebotomist Controller
 *
 * Endpoints: /api/v1/phlebotomists
 * Security: Mixed - some public, some ADMIN only
 */
@RestController
@RequestMapping("/api/v1/phlebotomists")
@RequiredArgsConstructor
@Tag(name = "Phlebotomist", description = "Phlebotomist profile management endpoints")
public class PhlebotomistController {

    private final PhlebotomistService phlebotomistService;

    /**
     * Create new phlebotomist (ADMIN only)
     *
     * @param request        Phlebotomist creation data
     * @param authentication Current admin user
     * @return PhlebotomistProfileResponse
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create phlebotomist", description = "Create new phlebotomist profile (ADMIN only)")
    public ResponseEntity<ApiResponse<PhlebotomistProfileResponse>> createPhlebotomist(
            @Valid @RequestBody CreatePhlebotomistRequest request,
            Authentication authentication) {

        String createdBy = authentication.getName();
        PhlebotomistProfileResponse response = phlebotomistService.createPhlebotomist(request, createdBy);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Phlebotomist created successfully"));
    }

    /**
     * Get current phlebotomist's profile
     *
     * @param authentication Current phlebotomist
     * @return PhlebotomistProfileResponse
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('PHLEBOTOMIST')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get my profile", description = "Get current phlebotomist's profile")
    public ResponseEntity<ApiResponse<PhlebotomistProfileResponse>> getMyProfile(
            Authentication authentication) {

        String email = authentication.getName();
        PhlebotomistProfileResponse response = phlebotomistService.getPhlebotomistProfileByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get phlebotomist by ID
     *
     * @param userId Phlebotomist user ID
     * @return PhlebotomistProfileResponse
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get phlebotomist by ID", description = "Get phlebotomist profile by user ID (ADMIN/DOCTOR)")
    public ResponseEntity<ApiResponse<PhlebotomistProfileResponse>> getPhlebotomistById(
            @PathVariable UUID userId) {

        PhlebotomistProfileResponse response = phlebotomistService.getPhlebotomistProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all active phlebotomists
     *
     * @return List of PhlebotomistProfileResponse
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all active phlebotomists", description = "Get all active phlebotomists (ADMIN/DOCTOR)")
    public ResponseEntity<ApiResponse<List<PhlebotomistProfileResponse>>> getActivePhlebotomists() {

        List<PhlebotomistProfileResponse> response = phlebotomistService.getAllActivePhlebotomists();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all phlebotomists (ADMIN only)
     *
     * @return List of all phlebotomists
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all phlebotomists", description = "Get all phlebotomists for admin management (ADMIN only)")
    public ResponseEntity<ApiResponse<List<PhlebotomistProfileResponse>>> getAllPhlebotomists() {

        List<PhlebotomistProfileResponse> response = phlebotomistService.getAllPhlebotomists();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Delete phlebotomist profile (ADMIN only)
     *
     * @param userId         Phlebotomist user ID
     * @param authentication Current admin
     * @return Success response
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete phlebotomist", description = "Soft delete phlebotomist profile (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deletePhlebotomist(
            @PathVariable UUID userId,
            Authentication authentication) {

        String deletedBy = authentication.getName();
        phlebotomistService.deletePhlebotomistProfile(userId, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "Phlebotomist profile deleted successfully"));
    }
}
