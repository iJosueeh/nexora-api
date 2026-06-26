package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.common.util.SlugUtils;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.exceptions.InvalidFileFormatException;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import com.nexora.core.domain.content.ports.FileStoragePort;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import com.nexora.core.infrastructure.storage.FileValidator;
import com.nexora.core.infrastructure.storage.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UploadResourceUseCase {

    private final AcademicResourceRepository academicResourceRepository;
    private final ResourceCategoryRepository resourceCategoryRepository;
    private final FileStoragePort fileStoragePort;
    private final FileValidator fileValidator;
    private final StorageProperties storageProperties;
    private final SecurityService securityService;

    public AcademicResource execute(MultipartFile file, String title, String description, UUID categoryId, String type) {
        UUID authorId = securityService.getCurrentUserId();

        validateInput(title, categoryId, type);

        resourceCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        validateFile(file);

        String key = uploadFile(file, authorId);

        String slug = generateUniqueSlug(title);

        AcademicResource resource = AcademicResource.builder()
                .slug(slug)
                .title(title.trim())
                .description(description != null ? description.trim() : null)
                .type(type)
                .categoryId(categoryId)
                .authorId(authorId)
                .fileUrl(key)
                .fileSize(file.getSize())
                .fileFormat(extractExtension(file.getOriginalFilename()))
                .averageRating(0.0)
                .ratingsCount(0)
                .downloadCount(0)
                .build();

        return academicResourceRepository.save(resource);
    }

    private void validateInput(String title, UUID categoryId, String type) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (title.trim().length() < 5 || title.trim().length() > 200) {
            throw new IllegalArgumentException("Title must be between 5 and 200 characters");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID is required");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Type is required");
        }
        if (!isValidType(type)) {
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

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        fileValidator.validate(file.getOriginalFilename(), file.getSize(), storageProperties.getMaxResourceSize());
    }

    private String uploadFile(MultipartFile file, UUID authorId) {
        try {
            String extension = fileValidator.extractExtension(file.getOriginalFilename());
            String key = authorId + "/" + UUID.randomUUID() + "." + extension;

            return fileStoragePort.upload(
                    storageProperties.getBucketResources(),
                    key,
                    file.getInputStream(),
                    file.getContentType(),
                    storageProperties.getMaxResourceSize()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content", e);
        }
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = SlugUtils.makeSlug(title);
        return baseSlug + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toUpperCase();
    }
}
