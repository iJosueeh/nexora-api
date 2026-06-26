package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.aggregates.ResourceRating;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import com.nexora.core.domain.content.ports.ResourceRatingRepository;
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
class RateResourceUseCaseTest {

    @Mock
    private ResourceRatingRepository resourceRatingRepository;

    @Mock
    private AcademicResourceRepository academicResourceRepository;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private RateResourceUseCase rateResourceUseCase;

    private UUID userId;
    private UUID resourceId;
    private AcademicResource resource;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        resourceId = UUID.randomUUID();

        resource = AcademicResource.builder()
                .id(resourceId)
                .slug("test-resource")
                .title("Test Resource")
                .type("GUIDE")
                .categoryId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .fileUrl("test.pdf")
                .fileSize(1024L)
                .fileFormat("PDF")
                .averageRating(0.0)
                .ratingsCount(0)
                .downloadCount(0)
                .build();

        lenient().when(securityService.getCurrentUserId()).thenReturn(userId);
        lenient().when(academicResourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
        lenient().when(resourceRatingRepository.countByResourceId(resourceId)).thenReturn(1);
        lenient().when(resourceRatingRepository.averageRatingByResourceId(resourceId)).thenReturn(4.0);
    }

    @Test
    void shouldCreateNewRating() {
        // Arrange
        when(resourceRatingRepository.findByUserIdAndResourceId(userId, resourceId)).thenReturn(Optional.empty());
        when(resourceRatingRepository.save(any(ResourceRating.class))).thenAnswer(invocation -> {
            ResourceRating rating = invocation.getArgument(0);
            return ResourceRating.builder()
                    .id(UUID.randomUUID())
                    .resourceId(rating.getResourceId())
                    .userId(rating.getUserId())
                    .rating(rating.getRating())
                    .build();
        });

        // Act
        ResourceRating result = rateResourceUseCase.execute(resourceId, 4);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getRating());
        assertEquals(resourceId, result.getResourceId());
        assertEquals(userId, result.getUserId());
        verify(resourceRatingRepository).save(any(ResourceRating.class));
        verify(academicResourceRepository).save(any(AcademicResource.class));
    }

    @Test
    void shouldUpdateExistingRating() {
        // Arrange
        ResourceRating existingRating = ResourceRating.builder()
                .id(UUID.randomUUID())
                .resourceId(resourceId)
                .userId(userId)
                .rating(3)
                .build();

        when(resourceRatingRepository.findByUserIdAndResourceId(userId, resourceId))
                .thenReturn(Optional.of(existingRating));
        when(resourceRatingRepository.save(any(ResourceRating.class))).thenAnswer(invocation -> {
            ResourceRating rating = invocation.getArgument(0);
            return ResourceRating.builder()
                    .id(rating.getId())
                    .resourceId(rating.getResourceId())
                    .userId(rating.getUserId())
                    .rating(rating.getRating())
                    .build();
        });

        // Act
        ResourceRating result = rateResourceUseCase.execute(resourceId, 5);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.getRating());
        assertEquals(existingRating.getId(), result.getId());
        verify(resourceRatingRepository).save(any(ResourceRating.class));
    }

    @Test
    void shouldThrowExceptionWhenRatingBelowOne() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                rateResourceUseCase.execute(resourceId, 0));
    }

    @Test
    void shouldThrowExceptionWhenRatingAboveFive() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                rateResourceUseCase.execute(resourceId, 6));
    }

    @Test
    void shouldThrowExceptionWhenResourceNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(academicResourceRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                rateResourceUseCase.execute(nonExistentId, 4));
    }

    @Test
    void shouldRecalculateStatsAfterRating() {
        // Arrange
        when(resourceRatingRepository.findByUserIdAndResourceId(userId, resourceId)).thenReturn(Optional.empty());
        when(resourceRatingRepository.save(any(ResourceRating.class))).thenAnswer(invocation -> {
            ResourceRating rating = invocation.getArgument(0);
            return ResourceRating.builder()
                    .id(UUID.randomUUID())
                    .resourceId(rating.getResourceId())
                    .userId(rating.getUserId())
                    .rating(rating.getRating())
                    .build();
        });

        when(resourceRatingRepository.countByResourceId(resourceId)).thenReturn(5);
        when(resourceRatingRepository.averageRatingByResourceId(resourceId)).thenReturn(4.2);

        // Act
        rateResourceUseCase.execute(resourceId, 4);

        // Assert
        verify(academicResourceRepository).save(argThat(r ->
                r.getRatingsCount() == 5 &&
                r.getAverageRating() == 4.2
        ));
    }

    @Test
    void shouldAcceptRatingExactlyOne() {
        // Arrange
        when(resourceRatingRepository.findByUserIdAndResourceId(userId, resourceId)).thenReturn(Optional.empty());
        when(resourceRatingRepository.save(any(ResourceRating.class))).thenAnswer(invocation -> {
            ResourceRating rating = invocation.getArgument(0);
            return ResourceRating.builder()
                    .id(UUID.randomUUID())
                    .resourceId(rating.getResourceId())
                    .userId(rating.getUserId())
                    .rating(rating.getRating())
                    .build();
        });

        // Act
        ResourceRating result = rateResourceUseCase.execute(resourceId, 1);

        // Assert
        assertEquals(1, result.getRating());
    }

    @Test
    void shouldAcceptRatingExactlyFive() {
        // Arrange
        when(resourceRatingRepository.findByUserIdAndResourceId(userId, resourceId)).thenReturn(Optional.empty());
        when(resourceRatingRepository.save(any(ResourceRating.class))).thenAnswer(invocation -> {
            ResourceRating rating = invocation.getArgument(0);
            return ResourceRating.builder()
                    .id(UUID.randomUUID())
                    .resourceId(rating.getResourceId())
                    .userId(rating.getUserId())
                    .rating(rating.getRating())
                    .build();
        });

        // Act
        ResourceRating result = rateResourceUseCase.execute(resourceId, 5);

        // Assert
        assertEquals(5, result.getRating());
    }
}
