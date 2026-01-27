package com.medibridge.user_service_medibridge.domain.repository;

import com.medibridge.user_service_medibridge.domain.entity.OrganizationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationSettingsRepository extends JpaRepository<OrganizationSettings, UUID> {

    Optional<OrganizationSettings> findByConfigKey(String configKey);
}
