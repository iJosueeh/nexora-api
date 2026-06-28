package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteResourceUseCase {

    private final AcademicResourceRepository academicResourceRepository;
    private final SecurityService securityService;

    public boolean execute(UUID id) {
        UUID currentUserId = securityService.getCurrentUserId();

        AcademicResource existing = academicResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!existing.getAuthorId().equals(currentUserId)) {
            throw new SecurityException("You can only delete your own resources");
        }

        AcademicResource softDeleted = AcademicResource.builder()
                .id(existing.getId())
                .slug(existing.getSlug())
                .title(existing.getTitle())
                .description(existing.getDescription())
                .type(existing.getType())
                .categoryId(existing.getCategoryId())
                .authorId(existing.getAuthorId())
                .fileUrl(existing.getFileUrl())
                .fileSize(existing.getFileSize())
                .fileFormat(existing.getFileFormat())
                .averageRating(existing.getAverageRating())
                .ratingsCount(existing.getRatingsCount())
                .downloadCount(existing.getDownloadCount())
                .deletedAt(OffsetDateTime.now())
                .build();

        academicResourceRepository.save(softDeleted);
        return true;
    }
}
