package com.medibridge.user_service_medibridge.exception.custom;

import java.time.LocalDateTime;

/**
 * Exception thrown when account is locked
 */
public class AccountLockedException extends RuntimeException {

    private final LocalDateTime lockedUntil;

    public AccountLockedException(LocalDateTime lockedUntil) {
        super("Account is locked until: " + lockedUntil);
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
}
