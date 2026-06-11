package com.nexora.core.domain.user.valueobjects;

import java.util.Objects;

public record SupabaseId(String value) {
    public SupabaseId {
        Objects.requireNonNull(value, "SupabaseId cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("SupabaseId cannot be empty");
        }
    }
}
