package com.nexora.core.application.content.usecases.resources.queries;

import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetResourceCategoriesUseCaseTest {

    @Mock
    private ResourceCategoryRepository resourceCategoryRepository;

    @InjectMocks
    private GetResourceCategoriesUseCase getResourceCategoriesUseCase;

    private UUID careerId;

    @BeforeEach
    void setUp() {
        careerId = UUID.randomUUID();
    }

    @Test
    void shouldReturnAllCategoriesWhenCareerIdIsNull() {
        List<ResourceCategory> expectedCategories = Arrays.asList(
                ResourceCategory.builder().id(UUID.randomUUID()).name("Math").careerId(careerId).build(),
                ResourceCategory.builder().id(UUID.randomUUID()).name("Physics").careerId(careerId).build()
        );
        when(resourceCategoryRepository.findAll()).thenReturn(expectedCategories);

        List<ResourceCategory> result = getResourceCategoriesUseCase.execute(null);

        assertEquals(expectedCategories.size(), result.size());
        verify(resourceCategoryRepository).findAll();
        verify(resourceCategoryRepository, never()).findAllByCareerId(any());
    }

    @Test
    void shouldReturnCategoriesByCareerId() {
        List<ResourceCategory> expectedCategories = Arrays.asList(
                ResourceCategory.builder().id(UUID.randomUUID()).name("Math").careerId(careerId).build()
        );
        when(resourceCategoryRepository.findAllByCareerId(careerId)).thenReturn(expectedCategories);

        List<ResourceCategory> result = getResourceCategoriesUseCase.execute(careerId);

        assertEquals(expectedCategories.size(), result.size());
        assertEquals("Math", result.get(0).getName());
        verify(resourceCategoryRepository).findAllByCareerId(careerId);
        verify(resourceCategoryRepository, never()).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoCategories() {
        when(resourceCategoryRepository.findAllByCareerId(careerId)).thenReturn(List.of());

        List<ResourceCategory> result = getResourceCategoriesUseCase.execute(careerId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoriesForCareer() {
        when(resourceCategoryRepository.findAllByCareerId(careerId)).thenReturn(List.of());

        List<ResourceCategory> result = getResourceCategoriesUseCase.execute(careerId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnMultipleCategoriesForSameCareer() {
        UUID sameCareerId = UUID.randomUUID();
        List<ResourceCategory> expectedCategories = Arrays.asList(
                ResourceCategory.builder().id(UUID.randomUUID()).name("Math").careerId(sameCareerId).build(),
                ResourceCategory.builder().id(UUID.randomUUID()).name("Physics").careerId(sameCareerId).build(),
                ResourceCategory.builder().id(UUID.randomUUID()).name("Chemistry").careerId(sameCareerId).build()
        );
        when(resourceCategoryRepository.findAllByCareerId(sameCareerId)).thenReturn(expectedCategories);

        List<ResourceCategory> result = getResourceCategoriesUseCase.execute(sameCareerId);

        assertEquals(3, result.size());
    }
}
