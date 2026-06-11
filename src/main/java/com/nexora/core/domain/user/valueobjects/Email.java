package com.nexora.core.domain.user.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public String username() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(0, atIndex) : value;
    }

    public String domain() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(atIndex + 1) : "";
    }

    public boolean isUtpDomain() {
        return value.endsWith("@utp.edu.pe");
    }
}
