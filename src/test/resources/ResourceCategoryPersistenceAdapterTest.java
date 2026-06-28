package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.infrastructure.persistence.content.entities.ResourceCategoryJpaEntity;
import com.nexora.core.infrastructure.persistence.content.repositories.ResourceCategoryJpaRepository;
import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceCategoryPersistenceAdapterTest {

    @Mock
    private ResourceCategoryJpaRepository repository;

    @InjectMocks
    private ResourceCategoryPersistenceAdapter adapter;

    @Test
    void shouldFindCategoryById() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        
        CourseJpaEntity mockCourse = new CourseJpaEntity();
        mockCourse.setId(courseId);

        ResourceCategoryJpaEntity entity = ResourceCategoryJpaEntity.builder()
                .name("Matemáticas")
                .carrera(mockCourse)
                .build();
        entity.setId(categoryId);

        when(repository.findById(categoryId)).thenReturn(Optional.of(entity));

        // Act
        Optional<ResourceCategory> result = adapter.findById(categoryId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(categoryId, result.get().getId());
        assertEquals("Matemáticas", result.get().getName());
        assertEquals(courseId, result.get().getCareerId());
        verify(repository, times(1)).findById(categoryId);
    }
}