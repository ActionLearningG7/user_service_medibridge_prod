package com.medibridge.user_service_medibridge.util.constant;

/**
 * User roles in the hospital management system.
 * 
 * PATIENT: Default role for self-registered users
 * DOCTOR: Medical professionals (created by ADMIN only)
 * ADMIN: System administrators with highest privileges
 */
public enum Role {
    PATIENT("ROLE_PATIENT", "Patient"),
    DOCTOR("ROLE_DOCTOR", "Doctor"),
    ADMIN("ROLE_ADMIN", "Administrator");

    private final String authority;
    private final String displayName;

    Role(String authority, String displayName) {
        this.authority = authority;
        this.displayName = displayName;
    }

    public String getAuthority() {
        return authority;
    }

    public String getDisplayName() {
        return displayName;
    }
}
