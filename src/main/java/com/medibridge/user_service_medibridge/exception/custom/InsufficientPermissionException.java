package com.medibridge.user_service_medibridge.exception.custom;

/**
 * Exception thrown when user has insufficient permissions
 */
public class InsufficientPermissionException extends RuntimeException {

    public InsufficientPermissionException() {
        super("Insufficient permissions to perform this action");
    }

    public InsufficientPermissionException(String message) {
        super(message);
    }

    public InsufficientPermissionException(String action, String requiredRole) {
        super(String.format("Insufficient permissions. Action '%s' requires role: %s", action, requiredRole));
    }
}
