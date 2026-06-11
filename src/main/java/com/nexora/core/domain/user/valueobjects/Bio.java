package com.nexora.core.domain.user.valueobjects;

import java.util.Objects;

public record Bio(String value) {
    private static final int MAX_LENGTH = 500;

    public Bio {
        Objects.requireNonNull(value, "Bio cannot be null");
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Bio cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    public boolean isBlank() {
        return value == null || value.isBlank();
    }
}
