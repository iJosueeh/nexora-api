package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateResourceCategoryUseCaseTest {

    @Mock
    private ResourceCategoryRepository resourceCategoryRepository;

    @InjectMocks
    private UpdateResourceCategoryUseCase updateResourceCategoryUseCase;

    private UUID categoryId;
    private UUID careerId;
    private ResourceCategory existingCategory;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        careerId = UUID.randomUUID();
        existingCategory = ResourceCategory.builder()
                .id(categoryId)
                .name("Old Name")
                .careerId(careerId)
                .build();
    }

    @Test
    void shouldUpdateCategorySuccessfully() {
        String newName = "New Name";
        when(resourceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(resourceCategoryRepository.existsByCareerIdAndNameAndIdNot(careerId, newName, categoryId)).thenReturn(false);
        when(resourceCategoryRepository.save(any(ResourceCategory.class)))
                .thenReturn(ResourceCategory.builder()
                        .id(categoryId)
                        .name(newName)
                        .careerId(careerId)
                        .build());

        ResourceCategory result = updateResourceCategoryUseCase.execute(categoryId, newName, null);

        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(careerId, result.getCareerId());
    }

    @Test
    void shouldUpdateCategoryNameAndCareerId() {
        String newName = "New Name";
        UUID newCareerId = UUID.randomUUID();
        when(resourceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(resourceCategoryRepository.existsByCareerIdAndNameAndIdNot(newCareerId, newName, categoryId)).thenReturn(false);
        when(resourceCategoryRepository.save(any(ResourceCategory.class)))
                .thenReturn(ResourceCategory.builder()
                        .id(categoryId)
                        .name(newName)
                        .careerId(newCareerId)
                        .build());

        ResourceCategory result = updateResourceCategoryUseCase.execute(categoryId, newName, newCareerId);

        assertEquals(newName, result.getName());
        assertEquals(newCareerId, result.getCareerId());
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(resourceCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                updateResourceCategoryUseCase.execute(categoryId, "New Name", null));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        when(resourceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));

        assertThrows(IllegalArgumentException.class, () ->
                updateResourceCategoryUseCase.execute(categoryId, "", null));
        assertThrows(IllegalArgumentException.class, () ->
                updateResourceCategoryUseCase.execute(categoryId, "   ", null));
        assertThrows(IllegalArgumentException.class, () ->
                updateResourceCategoryUseCase.execute(categoryId, null, null));
    }

    @Test
    void shouldThrowExceptionWhenDuplicateNameExists() {
        String newName = "Existing Name";
        when(resourceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(resourceCategoryRepository.existsByCareerIdAndNameAndIdNot(careerId, newName, categoryId)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                updateResourceCategoryUseCase.execute(categoryId, newName, null));

        assertTrue(exception.getMessage().contains(newName));
        verify(resourceCategoryRepository, never()).save(any());
    }

    @Test
    void shouldPreserveCareerIdWhenNotProvided() {
        String newName = "New Name";
        when(resourceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(resourceCategoryRepository.existsByCareerIdAndNameAndIdNot(careerId, newName, categoryId)).thenReturn(false);
        when(resourceCategoryRepository.save(any(ResourceCategory.class)))
                .thenReturn(ResourceCategory.builder()
                        .id(categoryId)
                        .name(newName)
                        .careerId(careerId)
                        .build());

        ResourceCategory result = updateResourceCategoryUseCase.execute(categoryId, newName, null);

        assertEquals(careerId, result.getCareerId());
    }
}
