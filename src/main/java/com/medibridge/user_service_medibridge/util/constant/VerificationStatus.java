package com.medibridge.user_service_medibridge.util.constant;

/**
 * Verification status for doctor credentials.
 * Compliance: Ensures only verified doctors can practice
 */
public enum VerificationStatus {
    PENDING("Pending Verification"),
    VERIFIED("Verified"),
    REJECTED("Rejected"),
    EXPIRED("Expired - Needs Renewal");

    private final String displayName;

    VerificationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
