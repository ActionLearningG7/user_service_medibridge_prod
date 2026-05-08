package com.medibridge.user_service_medibridge.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ambulance Driver Profile Entity
 * Extends User entity with driver-specific information
 * Tracks location, availability, and vehicle assignment
 */
@Entity
@Table(name = "ambulance_driver_profiles", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_organization_id", columnList = "organization_id"),
        @Index(name = "idx_license_number", columnList = "license_number", unique = true),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_on_duty_status", columnList = "on_duty_status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmbulanceDriverProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "profile_id", updatable = false, nullable = false)
    private UUID profileId;

    // One-to-One relationship with User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    // Personal Information
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    // Driver Information
    @Column(name = "license_number", nullable = false, unique = true, length = 50)
    private String licenseNumber;

    @Column(name = "license_expiry", nullable = false)
    private LocalDateTime licenseExpiry;

    @Column(name = "vehicle_registration_number", length = 20)
    private String vehicleRegistrationNumber;

    @Column(name = "ambulance_id")
    private UUID ambulanceId;

    // Service Area
    @Column(name = "service_area", length = 255)
    private String serviceArea;

    @Column(name = "home_base_latitude")
    private Double homeBaseLatitude;

    @Column(name = "home_base_longitude")
    private Double homeBaseLongitude;

    // Availability Status
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "on_duty_status", nullable = false, length = 20)
    @Builder.Default
    private String onDutyStatus = "OFF_DUTY"; // OFF_DUTY, ON_DUTY, RESPONDING, BUSY

    // Current Location (updated via location ping)
    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "last_location_update_time")
    private LocalDateTime lastLocationUpdateTime;

    // Emergency Contact
    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    // Metadata
    @Column(name = "certification_number", length = 50)
    private String certificationNumber;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "total_shifts_completed")
    @Builder.Default
    private Integer totalShiftsCompleted = 0;

    @Column(name = "total_incidents_handled")
    @Builder.Default
    private Integer totalIncidentsHandled = 0;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 5.0;

    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;
}
