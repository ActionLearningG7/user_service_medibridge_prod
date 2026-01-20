package com.medibridge.user_service_medibridge.util.constant;

/**
 * Medical specializations for doctor profiles.
 * Based on common hospital departments
 */
public enum Specialization {
    GENERAL_MEDICINE("General Medicine"),
    CARDIOLOGY("Cardiology"),
    NEUROLOGY("Neurology"),
    ORTHOPEDICS("Orthopedics"),
    PEDIATRICS("Pediatrics"),
    GYNECOLOGY("Gynecology"),
    DERMATOLOGY("Dermatology"),
    PSYCHIATRY("Psychiatry"),
    RADIOLOGY("Radiology"),
    ANESTHESIOLOGY("Anesthesiology"),
    SURGERY("General Surgery"),
    EMERGENCY_MEDICINE("Emergency Medicine"),
    ONCOLOGY("Oncology"),
    OPHTHALMOLOGY("Ophthalmology"),
    ENT("Ear, Nose & Throat"),
    UROLOGY("Urology"),
    NEPHROLOGY("Nephrology"),
    GASTROENTEROLOGY("Gastroenterology"),
    PULMONOLOGY("Pulmonology"),
    ENDOCRINOLOGY("Endocrinology");

    private final String displayName;

    Specialization(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
