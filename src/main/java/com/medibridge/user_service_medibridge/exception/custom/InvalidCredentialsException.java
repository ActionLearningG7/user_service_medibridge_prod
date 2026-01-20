package com.medibridge.user_service_medibridge.exception.custom;

/**
 * Exception thrown when login credentials are invalid
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email/username or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
