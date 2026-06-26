package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateResourceUseCaseTest {

    @Mock
    private AcademicResourceRepository academicResourceRepository;

    @Mock
    private ResourceCategoryRepository resourceCategoryRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private UpdateResourceUseCase updateResourceUseCase;

    private UUID authorId;
    private UUID resourceId;
    private UUID categoryId;
    private AcademicResource existingResource;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        existingResource = AcademicResource.builder()
                .id(resourceId)
                .slug("test-resource")
                .title("Original Title")
                .description("Original Description")
                .type("GUIDE")
                .categoryId(categoryId)
                .authorId(authorId)
                .fileUrl("test.pdf")
                .fileSize(1024L)
                .fileFormat("PDF")
                .averageRating(4.0)
                .ratingsCount(5)
                .downloadCount(10)
                .build();

        lenient().when(securityService.getCurrentUserId()).thenReturn(authorId);
        lenient().when(academicResourceRepository.findById(resourceId)).thenReturn(Optional.of(existingResource));
    }

    @Test
    void shouldUpdateResourceSuccessfully() {
        // Arrange
        ResourceCategory category = ResourceCategory.builder()
                .id(categoryId)
                .name("Test Category")
                .careerId(UUID.randomUUID())
                .build();
        when(resourceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(academicResourceRepository.save(any(AcademicResource.class))).thenReturn(existingResource);

        // Act
        AcademicResource result = updateResourceUseCase.execute(resourceId, "Updated Title", "Updated Desc", categoryId, "SUMMARY");

        // Assert
        assertNotNull(result);
        verify(academicResourceRepository).save(any(AcademicResource.class));
    }

    @Test
    void shouldThrowExceptionWhenResourceNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(academicResourceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                updateResourceUseCase.execute(nonExistentId, "Title", "Desc", categoryId, "GUIDE"));
    }

    @Test
    void shouldThrowExceptionWhenNotOwner() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        when(securityService.getCurrentUserId()).thenReturn(otherUserId);

        // Act & Assert
        assertThrows(SecurityException.class, () ->
                updateResourceUseCase.execute(resourceId, "Title", "Desc", categoryId, "GUIDE"));
    }

    @Test
    void shouldThrowExceptionWhenTitleTooShort() {
        // Act & Assert - pass null categoryId so validation happens before category lookup
        assertThrows(IllegalArgumentException.class, () ->
                updateResourceUseCase.execute(resourceId, "Hi", "Desc", null, "GUIDE"));
    }

    @Test
    void shouldThrowExceptionWhenInvalidType() {
        // Act & Assert - pass null categoryId so validation happens before category lookup
        assertThrows(IllegalArgumentException.class, () ->
                updateResourceUseCase.execute(resourceId, "Valid Title", "Desc", null, "INVALID"));
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        // Arrange
        when(academicResourceRepository.save(any(AcademicResource.class))).thenReturn(existingResource);

        // Act
        AcademicResource result = updateResourceUseCase.execute(resourceId, null, null, null, null);

        // Assert
        assertNotNull(result);
        verify(academicResourceRepository).save(argThat(r ->
                r.getTitle().equals("Original Title") &&
                r.getType().equals("GUIDE")
        ));
    }
}
