package com.nexora.core.application.content.usecases.resources.queries;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMyResourcesUseCaseTest {

    @Mock
    private AcademicResourceRepository academicResourceRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private GetMyResourcesUseCase getMyResourcesUseCase;

    private UUID authorId;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        when(securityService.getCurrentUserId()).thenReturn(authorId);
    }

    @Test
    void shouldReturnUserResources() {
        // Arrange
        AcademicResource resource = AcademicResource.builder()
                .id(UUID.randomUUID())
                .slug("my-resource")
                .title("My Resource")
                .type("GUIDE")
                .categoryId(UUID.randomUUID())
                .authorId(authorId)
                .fileUrl("test.pdf")
                .fileSize(1024L)
                .fileFormat("PDF")
                .averageRating(4.0)
                .ratingsCount(5)
                .downloadCount(10)
                .build();

        when(academicResourceRepository.findAllByAuthorId(authorId, 20, 0))
                .thenReturn(List.of(resource));

        // Act
        List<AcademicResource> result = getMyResourcesUseCase.execute(20, 0);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(authorId, result.get(0).getAuthorId());
        verify(academicResourceRepository).findAllByAuthorId(authorId, 20, 0);
    }

    @Test
    void shouldReturnEmptyListWhenNoResources() {
        // Arrange
        when(academicResourceRepository.findAllByAuthorId(authorId, 20, 0))
                .thenReturn(List.of());

        // Act
        List<AcademicResource> result = getMyResourcesUseCase.execute(20, 0);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldClampLimitToMinMax() {
        // Arrange
        when(academicResourceRepository.findAllByAuthorId(authorId, 100, 0))
                .thenReturn(List.of());

        // Act
        getMyResourcesUseCase.execute(200, 0);

        // Assert
        verify(academicResourceRepository).findAllByAuthorId(authorId, 100, 0);
    }

    @Test
    void shouldClampOffsetToZero() {
        // Arrange
        when(academicResourceRepository.findAllByAuthorId(authorId, 20, 0))
                .thenReturn(List.of());

        // Act
        getMyResourcesUseCase.execute(20, -5);

        // Assert
        verify(academicResourceRepository).findAllByAuthorId(authorId, 20, 0);
    }
}
