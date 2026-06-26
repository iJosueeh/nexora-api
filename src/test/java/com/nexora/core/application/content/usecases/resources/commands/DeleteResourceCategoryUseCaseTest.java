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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteResourceCategoryUseCaseTest {

    @Mock
    private ResourceCategoryRepository resourceCategoryRepository;

    @InjectMocks
    private DeleteResourceCategoryUseCase deleteResourceCategoryUseCase;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
    }

    @Test
    void shouldDeleteCategorySuccessfully() {
        when(resourceCategoryRepository.findById(categoryId))
                .thenReturn(Optional.of(ResourceCategory.builder()
                        .id(categoryId)
                        .name("Test Category")
                        .careerId(UUID.randomUUID())
                        .build()));

        boolean result = deleteResourceCategoryUseCase.execute(categoryId);

        assertTrue(result);
        verify(resourceCategoryRepository).deleteById(categoryId);
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(resourceCategoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                deleteResourceCategoryUseCase.execute(categoryId));
        verify(resourceCategoryRepository, never()).deleteById(any());
    }

    @Test
    void shouldReturnTrueWhenDeleted() {
        when(resourceCategoryRepository.findById(categoryId))
                .thenReturn(Optional.of(ResourceCategory.builder()
                        .id(categoryId)
                        .name("Test Category")
                        .careerId(UUID.randomUUID())
                        .build()));

        boolean result = deleteResourceCategoryUseCase.execute(categoryId);

        assertTrue(result);
    }
}
