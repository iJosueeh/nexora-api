package com.nexora.core.domain.user.valueobjects;

import java.util.Objects;
import java.util.UUID;

public record Career(UUID id, String name, UUID facultyId) {
    public Career {
        Objects.requireNonNull(id, "Career id cannot be null");
        Objects.requireNonNull(name, "Career name cannot be null");
        Objects.requireNonNull(facultyId, "Faculty id cannot be null");
    }
}
