package com.nexora.core.application.content.usecases.resources.queries;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import com.nexora.core.domain.content.ports.FileStoragePort;
import com.nexora.core.infrastructure.storage.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GenerateResourceDownloadUrlUseCase {

    private final AcademicResourceRepository academicResourceRepository;
    private final FileStoragePort fileStoragePort;
    private final StorageProperties storageProperties;
    private final SecurityService securityService;

    public String execute(UUID resourceId) {
        securityService.getCurrentUserId();

        AcademicResource resource = academicResourceRepository.findByIdNotDeleted(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        String downloadUrl = fileStoragePort.generatePresignedDownloadUrl(
                storageProperties.getBucketResources(),
                resource.getFileUrl(),
                storageProperties.getPresignedUrlExpiry()
        );

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
                .averageRating(resource.getAverageRating())
                .ratingsCount(resource.getRatingsCount())
                .downloadCount(resource.getDownloadCount() + 1)
                .deletedAt(resource.getDeletedAt())
                .build();

        academicResourceRepository.save(updated);

        return downloadUrl;
    }
}
