package com.nexora.core.application.content.usecases.resources.queries;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import com.nexora.core.domain.content.ports.FileStoragePort;
import com.nexora.core.infrastructure.storage.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateResourceDownloadUrlUseCaseTest {

    @Mock
    private AcademicResourceRepository academicResourceRepository;
    @Mock
    private FileStoragePort fileStoragePort;
    @Mock
    private StorageProperties storageProperties;
    @Mock
    private SecurityService securityService;

    @InjectMocks
    private GenerateResourceDownloadUrlUseCase generateResourceDownloadUrlUseCase;

    private UUID resourceId;
    private UUID userId;
    private AcademicResource mockResource;
    private String expectedUrl;

    @BeforeEach
    void setUp() {
        resourceId = UUID.randomUUID();
        userId = UUID.randomUUID();
        expectedUrl = "https://storage.example.com/download/file.pdf?token=abc123";

        mockResource = AcademicResource.builder()
                .id(resourceId)
                .slug("test-resource")
                .title("Test Resource")
                .description("Test description")
                .type("PDF")
                .categoryId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .fileUrl("resources/test.pdf")
                .fileSize(1024L)
                .fileFormat("pdf")
                .averageRating(4.0)
                .ratingsCount(10)
                .downloadCount(5)
                .build();

        lenient().when(securityService.getCurrentUserId()).thenReturn(userId);
        lenient().when(storageProperties.getBucketResources()).thenReturn("nexora-resources");
        lenient().when(storageProperties.getPresignedUrlExpiry()).thenReturn(Duration.ofMinutes(15));
    }

    @Test
    void execute_shouldReturnPresignedUrlAndIncrementDownloadCount() {
        when(academicResourceRepository.findByIdNotDeleted(resourceId))
                .thenReturn(Optional.of(mockResource));
        when(fileStoragePort.generatePresignedDownloadUrl(anyString(), anyString(), any(Duration.class)))
                .thenReturn(expectedUrl);
        when(academicResourceRepository.save(any())).thenReturn(mockResource);

        String result = generateResourceDownloadUrlUseCase.execute(resourceId);

        assertEquals(expectedUrl, result);
        verify(academicResourceRepository).save(argThat(r ->
            r.getDownloadCount() == 6
        ));
    }

    @Test
    void execute_shouldThrowExceptionWhenResourceNotFound() {
        when(academicResourceRepository.findByIdNotDeleted(resourceId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                generateResourceDownloadUrlUseCase.execute(resourceId));
    }

    @Test
    void execute_shouldCallFileStoragePortWithCorrectParameters() {
        when(academicResourceRepository.findByIdNotDeleted(resourceId))
                .thenReturn(Optional.of(mockResource));
        when(fileStoragePort.generatePresignedDownloadUrl(anyString(), anyString(), any(Duration.class)))
                .thenReturn(expectedUrl);
        when(academicResourceRepository.save(any())).thenReturn(mockResource);

        generateResourceDownloadUrlUseCase.execute(resourceId);

        verify(fileStoragePort).generatePresignedDownloadUrl(
                eq("nexora-resources"),
                eq("resources/test.pdf"),
                eq(Duration.ofMinutes(15))
        );
    }

    @Test
    void execute_shouldPreserveOtherFieldsWhenUpdatingDownloadCount() {
        when(academicResourceRepository.findByIdNotDeleted(resourceId))
                .thenReturn(Optional.of(mockResource));
        when(fileStoragePort.generatePresignedDownloadUrl(anyString(), anyString(), any(Duration.class)))
                .thenReturn(expectedUrl);
        when(academicResourceRepository.save(any())).thenReturn(mockResource);

        generateResourceDownloadUrlUseCase.execute(resourceId);

        verify(academicResourceRepository).save(argThat(r ->
            r.getTitle().equals("Test Resource") &&
            r.getAverageRating() == 4.0 &&
            r.getRatingsCount() == 10 &&
            r.getDownloadCount() == 6
        ));
    }

    @Test
    void execute_shouldThrowExceptionWhenUserNotAuthenticated() {
        when(securityService.getCurrentUserId()).thenThrow(new RuntimeException("Unauthorized"));

        assertThrows(RuntimeException.class, () ->
                generateResourceDownloadUrlUseCase.execute(resourceId));
    }

    @Test
    void execute_shouldStartDownloadCountFromZero() {
        AcademicResource resourceWithZeroDownloads = AcademicResource.builder()
                .id(resourceId)
                .slug("test-resource")
                .title("Test Resource")
                .description("Test description")
                .type("PDF")
                .categoryId(UUID.randomUUID())
                .authorId(UUID.randomUUID())
                .fileUrl("resources/test.pdf")
                .fileSize(1024L)
                .fileFormat("pdf")
                .averageRating(4.0)
                .ratingsCount(10)
                .downloadCount(0)
                .build();

        when(academicResourceRepository.findByIdNotDeleted(resourceId))
                .thenReturn(Optional.of(resourceWithZeroDownloads));
        when(fileStoragePort.generatePresignedDownloadUrl(anyString(), anyString(), any(Duration.class)))
                .thenReturn(expectedUrl);
        when(academicResourceRepository.save(any())).thenReturn(resourceWithZeroDownloads);

        String result = generateResourceDownloadUrlUseCase.execute(resourceId);

        assertEquals(expectedUrl, result);
        verify(academicResourceRepository).save(argThat(r ->
            r.getDownloadCount() == 1
        ));
    }
}
