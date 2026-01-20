package com.medibridge.user_service_medibridge.exception.custom;

/**
 * Exception thrown when token is invalid or expired
 */
public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Invalid or expired token");
    }

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String tokenType, String reason) {
        super(String.format("Invalid %s token: %s", tokenType, reason));
    }
}
