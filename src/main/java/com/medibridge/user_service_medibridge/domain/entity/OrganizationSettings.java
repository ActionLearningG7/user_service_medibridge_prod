package com.medibridge.user_service_medibridge.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity to store global organization settings/configuration
 * We expect only one row typically, or key-value based.
 * For now, specific fields for address.
 */
@Entity
@Table(name = "organization_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OrganizationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Singleton key or just rely on "LIMIT 1" logic.
    // Let's us a key to ensure uniqueness if we want multiple configs later.
    @Column(unique = true)
    private String configKey; // e.g., "MAIN_HOSPITAL"

    private String name;
    private String street;
    private String city;
    private String zipCode;
    private String country;

    // Geolocation
    private Double latitude;
    private Double longitude;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private String updatedBy;
}
