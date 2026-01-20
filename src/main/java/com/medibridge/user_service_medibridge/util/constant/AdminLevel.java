package com.medibridge.user_service_medibridge.util.constant;

/**
 * Administrative privilege levels.
 * Supports hierarchical admin permissions
 */
public enum AdminLevel {
    SUPER_ADMIN(1, "Super Administrator"),
    SYSTEM_ADMIN(2, "System Administrator"),
    DEPARTMENT_ADMIN(3, "Department Administrator"),
    SUPPORT_ADMIN(4, "Support Administrator");

    private final int level;
    private final String displayName;

    AdminLevel(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean hasHigherPrivilegeThan(AdminLevel other) {
        return this.level < other.level; // Lower number = higher privilege
    }
}
