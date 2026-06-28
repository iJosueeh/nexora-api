package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateResourceUseCase {

    private final AcademicResourceRepository academicResourceRepository;
    private final ResourceCategoryRepository resourceCategoryRepository;
    private final SecurityService securityService;

    public AcademicResource execute(UUID id, String title, String description, UUID categoryId, String type) {
        UUID currentUserId = securityService.getCurrentUserId();

        AcademicResource existing = academicResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!existing.getAuthorId().equals(currentUserId)) {
            throw new SecurityException("You can only update your own resources");
        }

        if (categoryId != null) {
            resourceCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
        }

        validateInput(title, type);

        AcademicResource updated = AcademicResource.builder()
                .id(existing.getId())
                .slug(existing.getSlug())
                .title(title != null ? title.trim() : existing.getTitle())
                .description(description != null ? description.trim() : existing.getDescription())
                .type(type != null ? type : existing.getType())
                .categoryId(categoryId != null ? categoryId : existing.getCategoryId())
                .authorId(existing.getAuthorId())
                .fileUrl(existing.getFileUrl())
                .fileSize(existing.getFileSize())
                .fileFormat(existing.getFileFormat())
                .averageRating(existing.getAverageRating())
                .ratingsCount(existing.getRatingsCount())
                .downloadCount(existing.getDownloadCount())
                .deletedAt(existing.getDeletedAt())
                .build();

        return academicResourceRepository.save(updated);
    }

    private void validateInput(String title, String type) {
        if (title != null && (title.trim().length() < 5 || title.trim().length() > 200)) {
            throw new IllegalArgumentException("Title must be between 5 and 200 characters");
        }
        if (type != null && !isValidType(type)) {
            throw new IllegalArgumentException("Invalid resource type: " + type);
        }
    }

    private boolean isValidType(String type) {
        try {
            ResourceTypeEnum.valueOf(type.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private enum ResourceTypeEnum {
        SUMMARY, GUIDE, FLASHCARD, EXAM, OTHER
    }
}
