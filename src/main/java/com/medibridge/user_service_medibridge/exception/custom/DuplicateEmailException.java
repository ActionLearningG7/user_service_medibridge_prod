package com.medibridge.user_service_medibridge.exception.custom;

/**
 * Exception thrown when attempting to create a user with duplicate email
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}
