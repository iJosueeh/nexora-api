package com.nexora.core.infrastructure.persistence.user.adapters;

import com.nexora.core.application.auth.ports.CourseCatalogPort;
import com.nexora.core.infrastructure.persistence.user.repositories.CourseRepository;
import lombok.RequiredArgsConstructor;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseCatalogAdapter implements CourseCatalogPort {

    private final CourseRepository courseRepository;

    @Override
    public Optional<CourseData> findByNameIgnoreCase(String name) {
        return courseRepository.findByNameIgnoreCase(name)
                .map(c -> new CourseData(c.getId(), c.getName(), c.getFacultad().getId()));
    }
}
