package com.nexora.core.application.content.usecases.resources.queries;

import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetResourceByIdUseCaseTest {

    @Mock
    private AcademicResourceRepository academicResourceRepository;

    @InjectMocks
    private GetResourceByIdUseCase getResourceByIdUseCase;

    @Test
    void shouldReturnResourceWhenExistsAndNotDeleted() {
        // Arrange
        UUID resourceId = UUID.randomUUID();
        AcademicResource resource = AcademicResource.builder()
                .id(resourceId)
                .slug("test-resource")
                .title("Test Resource")
                .type("GUIDE")
                .categoryId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .fileUrl("test.pdf")
                .fileSize(1024L)
                .fileFormat("PDF")
                .averageRating(4.0)
                .ratingsCount(5)
                .downloadCount(10)
                .deletedAt(null)
                .build();

        when(academicResourceRepository.findByIdNotDeleted(resourceId))
                .thenReturn(Optional.of(resource));

        // Act
        Optional<AcademicResource> result = getResourceByIdUseCase.execute(resourceId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(resourceId, result.get().getId());
        assertEquals("test-resource", result.get().getSlug());
        assertNull(result.get().getDeletedAt());
        verify(academicResourceRepository).findByIdNotDeleted(resourceId);
    }

    @Test
    void shouldReturnEmptyWhenResourceNotFound() {
        // Arrange
        UUID resourceId = UUID.randomUUID();
        when(academicResourceRepository.findByIdNotDeleted(resourceId))
                .thenReturn(Optional.empty());

        // Act
        Optional<AcademicResource> result = getResourceByIdUseCase.execute(resourceId);

        // Assert
        assertTrue(result.isEmpty());
        verify(academicResourceRepository).findByIdNotDeleted(resourceId);
    }

    @Test
    void shouldReturnEmptyWhenResourceIsSoftDeleted() {
        // Arrange
        UUID resourceId = UUID.randomUUID();
        when(academicResourceRepository.findByIdNotDeleted(resourceId))
                .thenReturn(Optional.empty());

        // Act
        Optional<AcademicResource> result = getResourceByIdUseCase.execute(resourceId);

        // Assert
        assertTrue(result.isEmpty());
        verify(academicResourceRepository).findByIdNotDeleted(resourceId);
    }
}
