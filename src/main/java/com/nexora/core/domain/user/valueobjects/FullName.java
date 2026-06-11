package com.nexora.core.domain.user.valueobjects;

import java.util.Objects;

public record FullName(String value) {
    public FullName {
        Objects.requireNonNull(value, "FullName cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("FullName cannot be empty");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("FullName cannot exceed 100 characters");
        }
    }

    public String firstName() {
        int spaceIndex = value.indexOf(' ');
        return spaceIndex > 0 ? value.substring(0, spaceIndex) : value;
    }

    public String lastName() {
        int spaceIndex = value.lastIndexOf(' ');
        return spaceIndex > 0 ? value.substring(spaceIndex + 1) : "";
    }
}
