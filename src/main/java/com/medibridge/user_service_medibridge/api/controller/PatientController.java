package com.medibridge.user_service_medibridge.api.controller;

import com.medibridge.user_service_medibridge.api.dto.request.UpdatePatientProfileRequest;
import com.medibridge.user_service_medibridge.api.dto.response.ApiResponse;
import com.medibridge.user_service_medibridge.api.dto.response.PatientProfileResponse;
import com.medibridge.user_service_medibridge.domain.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Patient Controller
 * 
 * Endpoints: /api/v1/patients
 * Security: PATIENT can access own data, ADMIN can access all
 */
@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Patient", description = "Patient profile management endpoints")
public class PatientController {

    private final PatientService patientService;

    /**
     * Get current patient's profile
     * 
     * @param authentication Current user
     * @return PatientProfileResponse
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get my profile", description = "Get current patient's profile")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getMyProfile(
            Authentication authentication) {

        String email = authentication.getName();
        PatientProfileResponse response = patientService.getPatientProfileByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update current patient's profile
     * 
     * @param request        Profile update data
     * @param authentication Current user
     * @return Updated PatientProfileResponse
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update my profile", description = "Update current patient's profile")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdatePatientProfileRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        PatientProfileResponse response = patientService.updatePatientProfileByEmail(email, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    /**
     * Get patient by ID (ADMIN or DOCTOR only)
     * 
     * @param userId Patient user ID
     * @return PatientProfileResponse
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get patient by ID", description = "Get patient profile by user ID (ADMIN/DOCTOR only)")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getPatientById(
            @PathVariable UUID userId) {

        PatientProfileResponse response = patientService.getPatientProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all patients (ADMIN only)
     * 
     * @return List of PatientProfileResponse
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all patients", description = "Get all patient profiles (ADMIN only)")
    public ResponseEntity<ApiResponse<List<PatientProfileResponse>>> getAllPatients() {

        List<PatientProfileResponse> response = patientService.getAllPatients();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search patients (ADMIN only)
     * 
     * @param searchTerm Search by name or phone
     * @return List of PatientProfileResponse
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search patients", description = "Search patients by name or phone (ADMIN only)")
    public ResponseEntity<ApiResponse<List<PatientProfileResponse>>> searchPatients(
            @RequestParam String searchTerm) {

        List<PatientProfileResponse> response = patientService.searchPatients(searchTerm);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get incomplete profiles (ADMIN only)
     * 
     * @return List of PatientProfileResponse
     */
    @GetMapping("/incomplete")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get incomplete profiles", description = "Get patients with incomplete profiles (ADMIN only)")
    public ResponseEntity<ApiResponse<List<PatientProfileResponse>>> getIncompleteProfiles() {

        List<PatientProfileResponse> response = patientService.getIncompleteProfiles();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get patients without consent (ADMIN only)
     * 
     * @return List of PatientProfileResponse
     */
    @GetMapping("/without-consent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get patients without consent", description = "Get patients without treatment consent (ADMIN only)")
    public ResponseEntity<ApiResponse<List<PatientProfileResponse>>> getPatientsWithoutConsent() {

        List<PatientProfileResponse> response = patientService.getPatientsWithoutConsent();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Delete patient profile (ADMIN only)
     * 
     * @param userId Patient user ID
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete patient", description = "Soft delete patient profile (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable UUID userId) {

        patientService.deletePatientProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Patient profile deleted successfully"));
    }
}
