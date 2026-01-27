package com.medibridge.user_service_medibridge.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAddressResponse {
    private String name;
    private String street;
    private String city;
    private String zipCode;
    private String country;
    private Double latitude;
    private Double longitude;
}
