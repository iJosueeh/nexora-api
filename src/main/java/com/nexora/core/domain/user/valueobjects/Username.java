package com.nexora.core.domain.user.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

public record Username(String value) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{5,30}$");

    public Username {
        Objects.requireNonNull(value, "Username cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (!USERNAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Username must be 5-30 characters, alphanumeric or underscore"
            );
        }
    }
}
