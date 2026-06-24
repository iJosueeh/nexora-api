package com.nexora.core.domain.user.valueobjects;

public enum UserRole {
    ROLE_STUDENT,
    ROLE_ADMIN,
    ROLE_OFFICIAL;

    public String authority() {
        return name();
    }
}
