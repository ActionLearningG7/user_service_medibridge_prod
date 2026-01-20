package com.medibridge.user_service_medibridge.config.properties;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration
 * 
 * Provides API documentation at /swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediBridge User Service API")
                        .version("1.0.0")
                        .description("Hospital Management System - User Service\n\n" +
                                "This service handles user authentication, patient profiles, doctor management, and administrative operations.\n\n"
                                +
                                "**Security:** JWT Bearer token authentication\n\n" +
                                "**Roles:** PATIENT, DOCTOR, ADMIN")
                        .contact(new Contact()
                                .name("MediBridge Development Team")
                                .email("dev@medibridge.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}
