package com.nexora.core.application.auth.ports;

import java.util.Optional;
import java.util.UUID;

public interface CourseCatalogPort {
    record CourseData(UUID id, String name, UUID facultyId) {}
    Optional<CourseData> findByNameIgnoreCase(String name);
}
