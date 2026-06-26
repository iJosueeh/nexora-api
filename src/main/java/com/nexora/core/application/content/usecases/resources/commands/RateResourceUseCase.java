package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.aggregates.ResourceRating;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import com.nexora.core.domain.content.ports.ResourceRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RateResourceUseCase {

    private final ResourceRatingRepository resourceRatingRepository;
    private final AcademicResourceRepository academicResourceRepository;
    private final SecurityService securityService;

    public ResourceRating execute(UUID resourceId, int rating) {
        UUID userId = securityService.getCurrentUserId();

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        AcademicResource resource = academicResourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        Optional<ResourceRating> existingRating = resourceRatingRepository.findByUserIdAndResourceId(userId, resourceId);

        ResourceRating savedRating;
        if (existingRating.isPresent()) {
            savedRating = ResourceRating.builder()
                    .id(existingRating.get().getId())
                    .resourceId(resourceId)
                    .userId(userId)
                    .rating(rating)
                    .build();
        } else {
            savedRating = ResourceRating.builder()
                    .resourceId(resourceId)
                    .userId(userId)
                    .rating(rating)
                    .build();
        }

        ResourceRating result = resourceRatingRepository.save(savedRating);

        recalculateResourceStats(resourceId);

        return result;
    }

    private void recalculateResourceStats(UUID resourceId) {
        int count = resourceRatingRepository.countByResourceId(resourceId);
        double average = resourceRatingRepository.averageRatingByResourceId(resourceId);

        AcademicResource resource = academicResourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        AcademicResource updated = AcademicResource.builder()
                .id(resource.getId())
                .slug(resource.getSlug())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .type(resource.getType())
                .categoryId(resource.getCategoryId())
                .authorId(resource.getAuthorId())
                .fileUrl(resource.getFileUrl())
                .fileSize(resource.getFileSize())
                .fileFormat(resource.getFileFormat())
                .averageRating(Math.round(average * 100.0) / 100.0)
                .ratingsCount(count)
                .downloadCount(resource.getDownloadCount())
                .deletedAt(resource.getDeletedAt())
                .build();

        academicResourceRepository.save(updated);
    }
}
