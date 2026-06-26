package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import org.junit.jupiter.api.BeforeEach;
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
class DeleteResourceUseCaseTest {

    @Mock
    private AcademicResourceRepository academicResourceRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private DeleteResourceUseCase deleteResourceUseCase;

    private UUID authorId;
    private UUID resourceId;
    private AcademicResource existingResource;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        resourceId = UUID.randomUUID();

        existingResource = AcademicResource.builder()
                .id(resourceId)
                .slug("test-resource")
                .title("Test Resource")
                .type("GUIDE")
                .categoryId(UUID.randomUUID())
                .authorId(authorId)
                .fileUrl("test.pdf")
                .fileSize(1024L)
                .fileFormat("PDF")
                .averageRating(4.0)
                .ratingsCount(5)
                .downloadCount(10)
                .deletedAt(null)
                .build();

        lenient().when(securityService.getCurrentUserId()).thenReturn(authorId);
        lenient().when(academicResourceRepository.findById(resourceId)).thenReturn(Optional.of(existingResource));
    }

    @Test
    void shouldSoftDeleteResourceSuccessfully() {
        // Arrange
        when(academicResourceRepository.save(any(AcademicResource.class))).thenReturn(existingResource);

        // Act
        boolean result = deleteResourceUseCase.execute(resourceId);

        // Assert
        assertTrue(result);
        verify(academicResourceRepository).save(argThat(r ->
                r.getDeletedAt() != null
        ));
    }

    @Test
    void shouldThrowExceptionWhenResourceNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(academicResourceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                deleteResourceUseCase.execute(nonExistentId));
    }

    @Test
    void shouldThrowExceptionWhenNotOwner() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        when(securityService.getCurrentUserId()).thenReturn(otherUserId);

        // Act & Assert
        assertThrows(SecurityException.class, () ->
                deleteResourceUseCase.execute(resourceId));
    }

    @Test
    void shouldSetDeletedAtTimestamp() {
        // Arrange
        when(academicResourceRepository.save(any(AcademicResource.class))).thenAnswer(invocation -> {
            AcademicResource saved = invocation.getArgument(0);
            assertNotNull(saved.getDeletedAt());
            return saved;
        });

        // Act
        deleteResourceUseCase.execute(resourceId);

        // Assert
        verify(academicResourceRepository).save(argThat(r ->
                r.getDeletedAt() != null &&
                r.getDeletedAt().isBefore(OffsetDateTime.now().plusSeconds(5))
        ));
    }
}
