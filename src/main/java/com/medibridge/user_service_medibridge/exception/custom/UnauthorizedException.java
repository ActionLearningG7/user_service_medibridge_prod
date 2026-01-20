package com.medibridge.user_service_medibridge.exception.custom;

/**
 * Exception thrown when user is not authorized to perform an action
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Unauthorized access");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
