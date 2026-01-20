package com.medibridge.user_service_medibridge.domain.service.impl;

import com.medibridge.user_service_medibridge.api.dto.response.AdminProfileResponse;
import com.medibridge.user_service_medibridge.domain.entity.AdminProfile;
import com.medibridge.user_service_medibridge.domain.entity.User;
import com.medibridge.user_service_medibridge.domain.repository.AdminProfileRepository;
import com.medibridge.user_service_medibridge.domain.repository.UserRepository;
import com.medibridge.user_service_medibridge.domain.service.AdminService;
import com.medibridge.user_service_medibridge.exception.custom.ProfileNotFoundException;
import com.medibridge.user_service_medibridge.exception.custom.UserNotFoundException;
import com.medibridge.user_service_medibridge.util.constant.AdminLevel;
import com.medibridge.user_service_medibridge.util.constant.UserStatus;
import com.medibridge.user_service_medibridge.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AdminService
 * 
 * Handles admin profile management and system-wide administrative tasks
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminProfileRepository adminProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminProfileResponse getAdminProfile(UUID userId) {
        log.info("Fetching admin profile for user: {}", userId);

        AdminProfile profile = adminProfileRepository.findByUserUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Admin", userId));

        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProfileResponse> getAllAdmins() {
        log.info("Fetching all admin profiles");

        List<AdminProfile> profiles = adminProfileRepository.findByDeletedFalse();

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProfileResponse> getAdminsByLevel(AdminLevel level) {
        log.info("Fetching admins by level: {}", level);

        List<AdminProfile> profiles = adminProfileRepository.findByAdminLevelAndDeletedFalse(level);

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProfileResponse> getAdminsWhoCanVerifyDoctors() {
        log.info("Fetching admins authorized to verify doctors");

        List<AdminProfile> profiles = adminProfileRepository.findByCanVerifyDoctorsTrueAndDeletedFalse();

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProfileResponse> getNonCompliantAdmins() {
        log.info("Fetching non-compliant admins");

        // Fetch admins who haven't completed training or acknowledged privacy
        List<AdminProfile> profiles = adminProfileRepository.findNonCompliantAdmins();

        return profiles.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateUserStatus(UUID userId, UserStatus status) {
        log.info("Updating user status for user: {} to {}", userId, status);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedBy(SecurityUtils.getCurrentUsername());

        userRepository.save(user);
        log.info("User status updated successfully");
    }

    @Override
    public void suspendAdmin(UUID adminId, String reason, String suspendedBy) {
        log.info("Suspending admin: {} by: {} for: {}", adminId, suspendedBy, reason);

        AdminProfile profile = adminProfileRepository.findByUserUserIdAndDeletedFalse(adminId)
                .orElseThrow(() -> new ProfileNotFoundException("Admin", adminId));

        profile.suspend(suspendedBy, reason);
        adminProfileRepository.save(profile);

        // Also update the user status
        User user = profile.getUser();
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        log.info("Admin suspended successfully");
    }

    @Override
    public void unsuspendAdmin(UUID adminId) {
        log.info("Unsuspending admin: {}", adminId);

        AdminProfile profile = adminProfileRepository.findByUserUserIdAndDeletedFalse(adminId)
                .orElseThrow(() -> new ProfileNotFoundException("Admin", adminId));

        profile.unsuspend();
        adminProfileRepository.save(profile);

        // Also update the user status
        User user = profile.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Admin unsuspended successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStatistics() {
        log.info("Fetching system statistics");

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("totalAdmins", adminProfileRepository.countByDeletedFalse());

        // Use repository methods for exact counts if available, otherwise just these
        // for now
        // This can be expanded with more specific repository count methods

        return stats;
    }

    @Override
    public void deleteAdminProfile(UUID userId) {
        log.info("Deleting admin profile for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        AdminProfile profile = adminProfileRepository.findByUserUserIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Admin", userId));

        // Soft delete using entity method
        profile.softDelete(SecurityUtils.getCurrentUsername());
        adminProfileRepository.save(profile);

        // Soft delete user
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setDeletedBy(SecurityUtils.getCurrentUsername());
        userRepository.save(user);

        log.info("Admin profile deleted successfully");
    }

    /**
     * Map AdminProfile entity to response DTO
     */
    private AdminProfileResponse mapToResponse(AdminProfile profile) {
        return AdminProfileResponse.builder()
                .adminProfileId(profile.getAdminProfileId())
                .userId(profile.getUser().getUserId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .adminLevel(profile.getAdminLevel())
                .department(profile.getDepartment())
                .designation(profile.getDesignation())
                .canCreateUsers(profile.getCanCreateUsers())
                .canModifyUsers(profile.getCanModifyUsers())
                .canDeleteUsers(profile.getCanDeleteUsers())
                .canCreateDoctors(profile.getCanCreateDoctors())
                .canVerifyDoctors(profile.getCanVerifyDoctors())
                .canManageAppointments(profile.getCanManageAppointments())
                .canViewReports(profile.getCanViewReports())
                .canManageSystemConfig(profile.getCanManageSystemConfig())
                .hipaaTrainingCompleted(profile.getHipaaTrainingCompleted())
                .dataPrivacyAcknowledged(profile.getDataPrivacyAcknowledged())
                .codeOfConductAccepted(profile.getCodeOfConductAccepted())
                .complianceStatus(profile.isFullyCompliant() ? "COMPLIANT" : "NON_COMPLIANT")
                .employeeId(profile.getEmployeeId())
                .joiningDate(profile.getJoiningDate())
                .reportingTo(profile.getReportingTo())
                .active(profile.getActive())
                .suspended(profile.getSuspended())
                .suspendedReason(profile.getSuspendedReason())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
