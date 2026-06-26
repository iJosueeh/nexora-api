package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateResourceCategoryUseCaseTest {

    @Mock
    private ResourceCategoryRepository resourceCategoryRepository;

    @InjectMocks
    private CreateResourceCategoryUseCase createResourceCategoryUseCase;

    private UUID careerId;

    @BeforeEach
    void setUp() {
        careerId = UUID.randomUUID();
    }

    @Test
    void shouldCreateCategorySuccessfully() {
        String categoryName = "Matemáticas";
        when(resourceCategoryRepository.existsByCareerIdAndName(careerId, categoryName)).thenReturn(false);
        when(resourceCategoryRepository.save(any(ResourceCategory.class)))
                .thenReturn(ResourceCategory.builder()
                        .id(UUID.randomUUID())
                        .name(categoryName)
                        .careerId(careerId)
                        .build());

        ResourceCategory result = createResourceCategoryUseCase.execute(categoryName, careerId);

        assertNotNull(result);
        assertEquals(categoryName, result.getName());
        assertEquals(careerId, result.getCareerId());
        verify(resourceCategoryRepository).save(any(ResourceCategory.class));
    }

    @Test
    void shouldTrimCategoryName() {
        String categoryName = "  Matemáticas  ";
        String expectedName = "Matemáticas";
        when(resourceCategoryRepository.existsByCareerIdAndName(careerId, expectedName)).thenReturn(false);
        when(resourceCategoryRepository.save(any(ResourceCategory.class)))
                .thenReturn(ResourceCategory.builder()
                        .id(UUID.randomUUID())
                        .name(expectedName)
                        .careerId(careerId)
                        .build());

        ResourceCategory result = createResourceCategoryUseCase.execute(categoryName, careerId);

        assertEquals(expectedName, result.getName());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                createResourceCategoryUseCase.execute("", careerId));
        assertThrows(IllegalArgumentException.class, () ->
                createResourceCategoryUseCase.execute("   ", careerId));
        assertThrows(IllegalArgumentException.class, () ->
                createResourceCategoryUseCase.execute(null, careerId));
    }

    @Test
    void shouldThrowExceptionWhenCareerIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                createResourceCategoryUseCase.execute("Matemáticas", null));
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {
        String categoryName = "Matemáticas";
        when(resourceCategoryRepository.existsByCareerIdAndName(careerId, categoryName)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                createResourceCategoryUseCase.execute(categoryName, careerId));

        assertTrue(exception.getMessage().contains(categoryName));
        verify(resourceCategoryRepository, never()).save(any());
    }
}
