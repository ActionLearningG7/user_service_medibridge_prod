package com.medibridge.user_service_medibridge.util.constant;

/**
 * Gender options for patient and doctor profiles.
 * Healthcare compliance: Supports diverse gender identities
 */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other"),
    PREFER_NOT_TO_SAY("Prefer not to say");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
