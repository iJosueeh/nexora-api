package com.nexora.core.application.auth.usecases.queries;

import com.nexora.core.application.auth.dto.RegistrationCatalogsResponse;
import com.nexora.core.infrastructure.persistence.user.entities.AcademicInterestJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.AcademicInterestRepository;
import com.nexora.core.infrastructure.persistence.user.repositories.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetRegistrationCatalogsUseCase {

    private final CourseRepository courseRepository;
    private final AcademicInterestRepository academicInterestRepository;

    public RegistrationCatalogsResponse execute() {
        List<String> careers = courseRepository.findAllByOrderByNameAsc()
                .stream()
                .map(CourseJpaEntity::getName)
                .toList();

        List<String> academicInterests = academicInterestRepository.findAllByOrderByNameAsc()
                .stream()
                .map(AcademicInterestJpaEntity::getName)
                .toList();

        return RegistrationCatalogsResponse.builder()
                .careers(careers)
                .academicInterests(academicInterests)
                .build();
    }
}
