package com.nexora.core.application.content.usecases.resources.queries;

import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
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
class GetResourcesUseCaseTest {

    @Mock
    private AcademicResourceRepository academicResourceRepository;

    @InjectMocks
    private GetResourcesUseCase getResourcesUseCase;

    @Test
    void shouldReturnResourcesWithNoFilters() {
        // Arrange
        UUID careerId = null;
        UUID categoryId = null;
        String type = null;
        UUID authorId = null;
        Double minRating = null;
        int limit = 20;
        int offset = 0;

        AcademicResource resource = AcademicResource.builder()
                .id(UUID.randomUUID())
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
                .build();

        when(academicResourceRepository.findAll(isNull(), isNull(), isNull(), isNull(), isNull(), eq(limit), eq(offset)))
                .thenReturn(List.of(resource));

        // Act
        List<AcademicResource> result = getResourcesUseCase.execute(careerId, categoryId, type, authorId, minRating, limit, offset);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("test-resource", result.get(0).getSlug());
        verify(academicResourceRepository).findAll(isNull(), isNull(), isNull(), isNull(), isNull(), eq(limit), eq(offset));
    }

    @Test
    void shouldFilterByCareerId() {
        // Arrange
        UUID careerId = UUID.randomUUID();
        int limit = 10;
        int offset = 0;

        when(academicResourceRepository.findAll(eq(careerId), isNull(), isNull(), isNull(), isNull(), eq(limit), eq(offset)))
                .thenReturn(List.of());

        // Act
        List<AcademicResource> result = getResourcesUseCase.execute(careerId, null, null, null, null, limit, offset);

        // Assert
        assertTrue(result.isEmpty());
        verify(academicResourceRepository).findAll(eq(careerId), isNull(), isNull(), isNull(), isNull(), eq(limit), eq(offset));
    }

    @Test
    void shouldFilterByCategoryId() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        int limit = 10;
        int offset = 0;

        when(academicResourceRepository.findAll(isNull(), eq(categoryId), isNull(), isNull(), isNull(), eq(limit), eq(offset)))
                .thenReturn(List.of());

        // Act
        List<AcademicResource> result = getResourcesUseCase.execute(null, categoryId, null, null, null, limit, offset);

        // Assert
        assertTrue(result.isEmpty());
        verify(academicResourceRepository).findAll(isNull(), eq(categoryId), isNull(), isNull(), isNull(), eq(limit), eq(offset));
    }

    @Test
    void shouldFilterByType() {
        // Arrange
        String type = "GUIDE";
        int limit = 10;
        int offset = 0;

        when(academicResourceRepository.findAll(isNull(), isNull(), eq(type), isNull(), isNull(), eq(limit), eq(offset)))
                .thenReturn(List.of());

        // Act
        List<AcademicResource> result = getResourcesUseCase.execute(null, null, type, null, null, limit, offset);

        // Assert
        assertTrue(result.isEmpty());
        verify(academicResourceRepository).findAll(isNull(), isNull(), eq(type), isNull(), isNull(), eq(limit), eq(offset));
    }

    @Test
    void shouldFilterByAuthorId() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        int limit = 10;
        int offset = 0;

        when(academicResourceRepository.findAll(isNull(), isNull(), isNull(), eq(authorId), isNull(), eq(limit), eq(offset)))
                .thenReturn(List.of());

        // Act
        List<AcademicResource> result = getResourcesUseCase.execute(null, null, null, authorId, null, limit, offset);

        // Assert
        assertTrue(result.isEmpty());
        verify(academicResourceRepository).findAll(isNull(), isNull(), isNull(), eq(authorId), isNull(), eq(limit), eq(offset));
    }

    @Test
    void shouldFilterByMinRating() {
        // Arrange
        Double minRating = 4.0;
        int limit = 10;
        int offset = 0;

        when(academicResourceRepository.findAll(isNull(), isNull(), isNull(), isNull(), eq(minRating), eq(limit), eq(offset)))
                .thenReturn(List.of());

        // Act
        List<AcademicResource> result = getResourcesUseCase.execute(null, null, null, null, minRating, limit, offset);

        // Assert
        assertTrue(result.isEmpty());
        verify(academicResourceRepository).findAll(isNull(), isNull(), isNull(), isNull(), eq(minRating), eq(limit), eq(offset));
    }

    @Test
    void shouldApplyMultipleFilters() {
        // Arrange
        UUID careerId = UUID.randomUUID();
        String type = "GUIDE";
        Double minRating = 3.5;
        int limit = 5;
        int offset = 0;

        when(academicResourceRepository.findAll(eq(careerId), isNull(), eq(type), isNull(), eq(minRating), eq(limit), eq(offset)))
                .thenReturn(List.of());

        // Act
        List<AcademicResource> result = getResourcesUseCase.execute(careerId, null, type, null, minRating, limit, offset);

        // Assert
        assertTrue(result.isEmpty());
        verify(academicResourceRepository).findAll(eq(careerId), isNull(), eq(type), isNull(), eq(minRating), eq(limit), eq(offset));
    }

    @Test
    void shouldClampLimitToMinMax() {
        // Arrange - limit exceeds max (100)
        int limit = 200;
        int offset = 0;

        when(academicResourceRepository.findAll(isNull(), isNull(), isNull(), isNull(), isNull(), eq(100), eq(offset)))
                .thenReturn(List.of());

        // Act
        List<AcademicResource> result = getResourcesUseCase.execute(null, null, null, null, null, limit, offset);

        // Assert
        verify(academicResourceRepository).findAll(isNull(), isNull(), isNull(), isNull(), isNull(), eq(100), eq(offset));
    }

    @Test
    void shouldClampOffsetToZero() {
        // Arrange - negative offset
        int limit = 10;
        int offset = -5;

        when(academicResourceRepository.findAll(isNull(), isNull(), isNull(), isNull(), isNull(), eq(limit), eq(0)))
                .thenReturn(List.of());

        // Act
        List<AcademicResource> result = getResourcesUseCase.execute(null, null, null, null, null, limit, offset);

        // Assert
        verify(academicResourceRepository).findAll(isNull(), isNull(), isNull(), isNull(), isNull(), eq(limit), eq(0));
    }
}
