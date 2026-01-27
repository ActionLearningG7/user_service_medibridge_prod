package com.medibridge.user_service_medibridge.api.controller;

import com.medibridge.user_service_medibridge.api.dto.request.CreatePhlebotomistRequest;
import com.medibridge.user_service_medibridge.api.dto.request.UpdatePhlebotomistRequest;
import com.medibridge.user_service_medibridge.api.dto.response.ApiResponse;
import com.medibridge.user_service_medibridge.api.dto.response.PhlebotomistProfileResponse;
import com.medibridge.user_service_medibridge.domain.service.PhlebotomistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin Phlebotomist Management Controller
 *
 * Endpoints: /api/v1/admin/phlebotomists
 * Security: ADMIN only - highest privilege operations
 */
@RestController
@RequestMapping("/api/v1/admin/phlebotomists")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin - Phlebotomist Management", description = "Admin operations for phlebotomist management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPhlebotomistController {

    private final PhlebotomistService phlebotomistService;

    /**
     * POST /api/v1/admin/phlebotomists
     * Create new phlebotomist
     */
    @PostMapping
    @Operation(summary = "Create phlebotomist", description = "Create new phlebotomist account (ADMIN only)")
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
     * GET /api/v1/admin/phlebotomists
     * Get all phlebotomists with pagination and filtering
     */
    @GetMapping
    @Operation(summary = "Get all phlebotomists", description = "Get all phlebotomists with pagination and filters (ADMIN only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllPhlebotomists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.Direction.valueOf(sortDirection),
                sortBy
        );

        Page<PhlebotomistProfileResponse> pageResult = phlebotomistService.getAllPhlebotomistsPageable(
                pageable,
                search,
                status
        );

        Map<String, Object> response = new HashMap<>();
        response.put("content", pageResult.getContent());
        response.put("totalElements", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("currentPage", pageResult.getNumber());
        response.put("pageSize", pageResult.getSize());
        response.put("hasNext", pageResult.hasNext());
        response.put("hasPrevious", pageResult.hasPrevious());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/admin/phlebotomists/available
     * Get available phlebotomists for task assignment
     * IMPORTANT: This must come BEFORE /{id} to avoid path variable shadowing
     */
    @GetMapping("/available")
    @Operation(summary = "Get available phlebotomists", description = "Get available active phlebotomists for task assignment (ADMIN only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailablePhlebotomists() {

        List<PhlebotomistProfileResponse> phlebotomists = phlebotomistService.getAllActivePhlebotomists();

        Map<String, Object> response = new HashMap<>();
        response.put("content", phlebotomists);
        response.put("total", phlebotomists.size());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * GET /api/v1/admin/phlebotomists/{id}
     * Get phlebotomist details by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get phlebotomist details", description = "Get specific phlebotomist details (ADMIN only)")
    public ResponseEntity<ApiResponse<PhlebotomistProfileResponse>> getPhlebotomistById(
            @PathVariable UUID id) {

        PhlebotomistProfileResponse response = phlebotomistService.getPhlebotomistProfile(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * PUT /api/v1/admin/phlebotomists/{id}
     * Update phlebotomist information
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update phlebotomist", description = "Update phlebotomist information (ADMIN only)")
    public ResponseEntity<ApiResponse<PhlebotomistProfileResponse>> updatePhlebotomist(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePhlebotomistRequest request,
            Authentication authentication) {

        String updatedBy = authentication.getName();
        PhlebotomistProfileResponse response = phlebotomistService.updatePhlebotomist(id, request, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Phlebotomist updated successfully"));
    }

    /**
     * PATCH /api/v1/admin/phlebotomists/{id}/status
     * Update phlebotomist status (ACTIVE/INACTIVE)
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update phlebotomist status", description = "Update phlebotomist status to ACTIVE or INACTIVE (ADMIN only)")
    public ResponseEntity<ApiResponse<PhlebotomistProfileResponse>> updatePhlebotomistStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> statusRequest,
            Authentication authentication) {

        String status = statusRequest.get("status");
        if (status == null || (!status.equals("ACTIVE") && !status.equals("INACTIVE"))) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Invalid status. Must be ACTIVE or INACTIVE"));
        }

        String updatedBy = authentication.getName();
        PhlebotomistProfileResponse response = phlebotomistService.updatePhlebotomistStatus(id, status, updatedBy);
        return ResponseEntity.ok(ApiResponse.success(response, "Status updated successfully"));
    }

    /**
     * POST /api/v1/admin/phlebotomists/{id}/reset-credentials
     * Reset phlebotomist credentials (password)
     */
    @PostMapping("/{id}/reset-credentials")
    @Operation(summary = "Reset credentials", description = "Reset phlebotomist password and send new credentials (ADMIN only)")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPhlebotomistCredentials(
            @PathVariable UUID id,
            Authentication authentication) {

        String resetBy = authentication.getName();
        phlebotomistService.resetPhlebotomistCredentials(id, resetBy);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Credentials reset successfully. New password sent to email.");


        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * DELETE /api/v1/admin/phlebotomists/{id}
     * Soft delete phlebotomist
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete phlebotomist", description = "Soft delete phlebotomist profile (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deletePhlebotomist(
            @PathVariable UUID id,
            Authentication authentication) {

        String deletedBy = authentication.getName();
        phlebotomistService.deletePhlebotomistProfile(id, deletedBy);
        return ResponseEntity.ok(ApiResponse.success(null, "Phlebotomist deleted successfully"));
    }
}
