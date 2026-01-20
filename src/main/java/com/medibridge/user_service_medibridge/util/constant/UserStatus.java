package com.medibridge.user_service_medibridge.util.constant;

/**
 * User account status lifecycle.
 * 
 * Compliance: Supports verification workflows and account management
 */
public enum UserStatus {
    PENDING_VERIFICATION("Pending Email Verification"),
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    INACTIVE("Inactive"),
    DELETED("Deleted");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
