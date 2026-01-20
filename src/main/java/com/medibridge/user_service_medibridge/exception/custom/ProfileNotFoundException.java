package com.medibridge.user_service_medibridge.exception.custom;

import java.util.UUID;

/**
 * Exception thrown when a profile is not found
 */
public class ProfileNotFoundException extends RuntimeException {

    public ProfileNotFoundException(String profileType, UUID userId) {
        super(String.format("%s profile not found for user ID: %s", profileType, userId));
    }

    public ProfileNotFoundException(String message) {
        super(message);
    }
}
