package com.medibridge.user_service_medibridge.api.controller;

import com.medibridge.user_service_medibridge.api.dto.request.OrganizationAddressRequest;
import com.medibridge.user_service_medibridge.api.dto.response.ApiResponse;
import com.medibridge.user_service_medibridge.api.dto.response.OrganizationAddressResponse;
import com.medibridge.user_service_medibridge.domain.service.OrganizationService;
import com.medibridge.user_service_medibridge.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organization")
@RequiredArgsConstructor
@Tag(name = "Organization", description = "Organization details and configuration")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping("/address")
    @Operation(summary = "Get hospital address", description = "Get the main hospital/organization address")
    public ResponseEntity<ApiResponse<OrganizationAddressResponse>> getHospitalAddress() {
        OrganizationAddressResponse address = organizationService.getAddress();
        return ResponseEntity.ok(ApiResponse.success(address));
    }

    @PutMapping("/address")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update hospital address", description = "Update the main hospital/organization address (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> updateHospitalAddress(
            @RequestBody @Valid OrganizationAddressRequest request) {

        String currentUserId = SecurityUtils.getCurrentUserId();
        organizationService.updateAddress(request, currentUserId);

        return ResponseEntity.ok(ApiResponse.success(null, "Address updated successfully"));
    }
}
