package com.medibridge.user_service_medibridge.domain.service;

import com.medibridge.user_service_medibridge.api.dto.request.OrganizationAddressRequest;
import com.medibridge.user_service_medibridge.api.dto.response.OrganizationAddressResponse;
import com.medibridge.user_service_medibridge.domain.entity.OrganizationSettings;
import com.medibridge.user_service_medibridge.domain.repository.OrganizationSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationSettingsRepository repository;
    private static final String DEFAULT_KEY = "MAIN_HOSPITAL";

    public OrganizationAddressResponse getAddress() {
        return repository.findByConfigKey(DEFAULT_KEY)
                .map(this::toResponse)
                .orElseGet(() -> {
                    // Default fallback if not configured yet (as per Requirement for Le
                    // Kremlin-Bicêtre)
                    return OrganizationAddressResponse.builder()
                            .name("MediBridge Hospital")
                            .street("14-16 Rue Voltaire")
                            .city("Le Kremlin-Bicêtre")
                            .zipCode("94270")
                            .country("France")
                            .latitude(48.813893)
                            .longitude(2.365315)
                            .build();
                });
    }

    @Transactional
    public void updateAddress(OrganizationAddressRequest request, String updatedBy) {
        OrganizationSettings settings = repository.findByConfigKey(DEFAULT_KEY)
                .orElse(OrganizationSettings.builder().configKey(DEFAULT_KEY).build());

        settings.setName(request.getName());
        settings.setStreet(request.getStreet());
        settings.setCity(request.getCity());
        settings.setZipCode(request.getZipCode());
        settings.setCountry(request.getCountry());
        settings.setLatitude(request.getLatitude());
        settings.setLongitude(request.getLongitude());
        settings.setUpdatedBy(updatedBy);

        repository.save(settings);
    }

    private OrganizationAddressResponse toResponse(OrganizationSettings entity) {
        return OrganizationAddressResponse.builder()
                .name(entity.getName())
                .street(entity.getStreet())
                .city(entity.getCity())
                .zipCode(entity.getZipCode())
                .country(entity.getCountry())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }
}
