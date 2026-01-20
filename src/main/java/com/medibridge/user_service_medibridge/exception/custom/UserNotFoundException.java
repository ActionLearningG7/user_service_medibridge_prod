package com.medibridge.user_service_medibridge.exception.custom;

import java.util.UUID;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(UUID userId) {
        super("User not found with ID: " + userId);
    }

    public UserNotFoundException(String field, String value) {
        super(String.format("User not found with %s: %s", field, value));
    }
}
