package com.medibridge.user_service_medibridge.api.controller;

import com.medibridge.user_service_medibridge.domain.entity.AmbulanceDriverProfile;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.domain.repository.AmbulanceDriverProfileRepository;
import com.medibridge.user_service_medibridge.domain.repository.UserRepository;
import com.medibridge.user_service_medibridge.util.constant.Role;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin Controller for Ambulance Driver Management
 * Provides APIs for creating, updating, and managing ambulance drivers
 */
@RestController
@RequestMapping("/admin/ambulance-drivers")
@RequiredArgsConstructor
public class AdminAmbulanceDriverController {

    private final AmbulanceDriverProfileRepository driverProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new ambulance driver
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponse> createDriver(@RequestBody CreateDriverRequest request) {
        // Create user account
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.AMBULANCE_DRIVER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        user = userRepository.save(user);

        // Create driver profile
        AmbulanceDriverProfile profile = AmbulanceDriverProfile.builder()
                .user(user)
                .organizationId(request.getOrganizationId())
                .licenseNumber(request.getLicenseNumber())
                .licenseExpiry(request.getLicenseExpiry())
                .vehicleRegistrationNumber(request.getVehicleRegistrationNumber())
                .serviceArea(request.getServiceArea())
                .homeBaseLatitude(request.getHomeBaseLatitude())
                .homeBaseLongitude(request.getHomeBaseLongitude())
                .isActive(true)
                .onDutyStatus("OFF_DUTY")
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .certificationNumber(request.getCertificationNumber())
                .yearsOfExperience(request.getYearsOfExperience())
                .build();

        profile = driverProfileRepository.save(profile);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapToResponse(user, profile));
    }

    /**
     * Get all ambulance drivers with pagination
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<DriverResponse>> getAllDrivers(
            @RequestParam UUID organizationId,
            Pageable pageable) {

        Page<AmbulanceDriverProfile> profiles = driverProfileRepository.findAll(pageable);
        Page<DriverResponse> response = profiles.map(profile -> {
            assert profile.getUser().getId() != null;
            User user = userRepository.findById(profile.getUser().getUserId()).orElse(null);
            return mapToResponse(user, profile);
        });

        return ResponseEntity.ok(response);
    }

    /**
     * Get driver by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponse> getDriver(@PathVariable UUID id) {
        AmbulanceDriverProfile profile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        User user = userRepository.findById(profile.getUser().getUserId()).orElse(null);
        return ResponseEntity.ok(mapToResponse(user, profile));
    }

    /**
     * Update driver profile
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponse> updateDriver(
            @PathVariable UUID id,
            @RequestBody UpdateDriverRequest request) {

        AmbulanceDriverProfile profile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        // Update profile fields
        if (request.getLicenseNumber() != null) {
            profile.setLicenseNumber(request.getLicenseNumber());
        }
        if (request.getLicenseExpiry() != null) {
            profile.setLicenseExpiry(request.getLicenseExpiry());
        }
        if (request.getVehicleRegistrationNumber() != null) {
            profile.setVehicleRegistrationNumber(request.getVehicleRegistrationNumber());
        }
        if (request.getServiceArea() != null) {
            profile.setServiceArea(request.getServiceArea());
        }
        if (request.getHomeBaseLatitude() != null) {
            profile.setHomeBaseLatitude(request.getHomeBaseLatitude());
        }
        if (request.getHomeBaseLongitude() != null) {
            profile.setHomeBaseLongitude(request.getHomeBaseLongitude());
        }
        if (request.getIsActive() != null) {
            profile.setIsActive(request.getIsActive());
        }
        if (request.getOnDutyStatus() != null) {
            profile.setOnDutyStatus(request.getOnDutyStatus());
        }

        profile = driverProfileRepository.save(profile);
        User user = userRepository.findById(profile.getUser().getUserId()).orElse(null);

        return ResponseEntity.ok(mapToResponse(user, profile));
    }

    /**
     * Partial update (PATCH) for driver profile
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DriverResponse> patchDriver(
            @PathVariable UUID id,
            @RequestBody PatchDriverRequest request) {

        AmbulanceDriverProfile profile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        // Selectively update fields
        if (request.getOnDutyStatus() != null) {
            profile.setOnDutyStatus(request.getOnDutyStatus());
        }
        if (request.getIsActive() != null) {
            profile.setIsActive(request.getIsActive());
        }
        if (request.getAmbulanceId() != null) {
            profile.setAmbulanceId(request.getAmbulanceId());
        }

        profile = driverProfileRepository.save(profile);
        User user = userRepository.findById(profile.getUser().getUserId()).orElse(null);

        return ResponseEntity.ok(mapToResponse(user, profile));
    }

    /**
     * Delete driver (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDriver(@PathVariable UUID id) {
        AmbulanceDriverProfile profile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        profile.setIsActive(false);
        driverProfileRepository.save(profile);

        // Also deactivate user account
        User user = userRepository.findById(profile.getUser().getUserId()).orElse(null);
        if (user != null) {
            user.setStatus(UserStatus.INACTIVE);
            userRepository.save(user);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Reset driver credentials (password)
     */
    @PostMapping("/{id}/reset-credentials")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CredentialResetResponse> resetCredentials(
            @PathVariable UUID id,
            @RequestBody CredentialResetRequest request) {

        AmbulanceDriverProfile profile = driverProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        User user = userRepository.findById(profile.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate temporary password
        String tempPassword = generateTemporaryPassword();

        // Update user password
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);

        return ResponseEntity.ok(new CredentialResetResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                tempPassword
        ));
    }

    /**
     * Helper method to map entities to response
     */
    private DriverResponse mapToResponse(User user, AmbulanceDriverProfile profile) {
        return new DriverResponse(
                profile.getProfileId(),
                user != null ? user.getUserId() : null,
                user != null ? user.getUsername() : null,
                user != null ? user.getEmail() : null,
                user != null ? user.getRole() : null,
                profile.getLicenseNumber(),
                profile.getLicenseExpiry(),
                profile.getVehicleRegistrationNumber(),
                profile.getAmbulanceId(),
                profile.getServiceArea(),
                profile.getIsActive(),
                profile.getOnDutyStatus(),
                profile.getCurrentLatitude(),
                profile.getCurrentLongitude(),
                profile.getTotalIncidentsHandled(),
                profile.getAverageRating()
        );
    }

    /**
     * Generate random temporary password
     */
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().substring(0, 12);
    }

    // ===== DTOs =====

    @Data
    @AllArgsConstructor
    public static class CreateDriverRequest {
        private String username;
        private String email;
        private String password;
        private UUID organizationId;
        private String licenseNumber;
        private LocalDateTime licenseExpiry;
        private String vehicleRegistrationNumber;
        private String serviceArea;
        private Double homeBaseLatitude;
        private Double homeBaseLongitude;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String certificationNumber;
        private Integer yearsOfExperience;
    }

    @Data
    @AllArgsConstructor
    public static class UpdateDriverRequest {
        private String licenseNumber;
        private LocalDateTime licenseExpiry;
        private String vehicleRegistrationNumber;
        private String serviceArea;
        private Double homeBaseLatitude;
        private Double homeBaseLongitude;
        private Boolean isActive;
        private String onDutyStatus;
    }

    @Data
    @AllArgsConstructor
    public static class PatchDriverRequest {
        private String onDutyStatus;
        private Boolean isActive;
        private UUID ambulanceId;
    }

    @Data
    @AllArgsConstructor
    public static class CredentialResetRequest {
        // Can extend with additional fields if needed
    }

    @Data
    @AllArgsConstructor
    public static class CredentialResetResponse {
        private UUID userId;
        private String username;
        private String email;
        private String temporaryPassword;
    }

    @Data
    @AllArgsConstructor
    public static class DriverResponse {
        private UUID profileId;
        private UUID userId;
        private String username;
        private String email;
        private Role role;
        private String licenseNumber;
        private LocalDateTime licenseExpiry;
        private String vehicleRegistrationNumber;
        private UUID ambulanceId;
        private String serviceArea;
        private Boolean isActive;
        private String onDutyStatus;
        private Double currentLatitude;
        private Double currentLongitude;
        private Integer totalIncidentsHandled;
        private Double averageRating;
    }
}
