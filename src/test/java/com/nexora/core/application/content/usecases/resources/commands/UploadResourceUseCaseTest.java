package com.nexora.core.application.content.usecases.resources.commands;

import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.domain.content.aggregates.AcademicResource;
import com.nexora.core.domain.content.aggregates.ResourceCategory;
import com.nexora.core.domain.content.exceptions.FileTooLargeException;
import com.nexora.core.domain.content.exceptions.InvalidFileFormatException;
import com.nexora.core.domain.content.ports.AcademicResourceRepository;
import com.nexora.core.domain.content.ports.FileStoragePort;
import com.nexora.core.domain.content.ports.ResourceCategoryRepository;
import com.nexora.core.infrastructure.storage.FileValidator;
import com.nexora.core.infrastructure.storage.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadResourceUseCaseTest {

    @Mock
    private AcademicResourceRepository academicResourceRepository;

    @Mock
    private ResourceCategoryRepository resourceCategoryRepository;

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private FileValidator fileValidator;

    @Mock
    private StorageProperties storageProperties;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private UploadResourceUseCase uploadResourceUseCase;

    private UUID authorId;
    private UUID categoryId;
    private ResourceCategory category;
    private StorageProperties properties;

    @BeforeEach
    void setUp() {
        authorId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        category = ResourceCategory.builder()
                .id(categoryId)
                .name("Test Category")
                .careerId(UUID.randomUUID())
                .build();

        properties = new StorageProperties();
        properties.setMaxResourceSize(20971520L);
        properties.setBuckets(java.util.Map.of("resources", "nexora-resources"));

        lenient().when(securityService.getCurrentUserId()).thenReturn(authorId);
        lenient().when(resourceCategoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        lenient().doNothing().when(fileValidator).validate(anyString(), anyLong(), anyLong());
        lenient().when(fileStoragePort.upload(anyString(), anyString(), any(), anyString(), anyLong()))
                .thenReturn(authorId + "/test-file.pdf");
    }

    @Test
    void shouldUploadValidPdfFile() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                new ByteArrayInputStream("test content".getBytes())
        );

        AcademicResource savedResource = AcademicResource.builder()
                .id(UUID.randomUUID())
                .slug("test-document-abc123")
                .title("Test Document")
                .type("GUIDE")
                .categoryId(categoryId)
                .authorId(authorId)
                .fileUrl(authorId + "/test-file.pdf")
                .fileSize(12L)
                .fileFormat("PDF")
                .averageRating(0.0)
                .ratingsCount(0)
                .downloadCount(0)
                .build();

        when(academicResourceRepository.save(any(AcademicResource.class))).thenReturn(savedResource);

        // Act
        AcademicResource result = uploadResourceUseCase.execute(
                file, "Test Document", "A test document", categoryId, "GUIDE");

        // Assert
        assertNotNull(result);
        assertEquals("Test Document", result.getTitle());
        assertEquals(categoryId, result.getCategoryId());
        assertEquals(authorId, result.getAuthorId());
        verify(academicResourceRepository).save(any(AcademicResource.class));
    }

    @Test
    void shouldThrowExceptionWhenFileTooLarge() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-file.pdf",
                "application/pdf",
                new ByteArrayInputStream("test content".getBytes())
        );

        doThrow(new FileTooLargeException(20971520L))
                .when(fileValidator).validate(anyString(), anyLong(), anyLong());

        // Act & Assert
        assertThrows(FileTooLargeException.class, () ->
                uploadResourceUseCase.execute(file, "Test Document", "Desc", categoryId, "GUIDE"));
    }

    @Test
    void shouldThrowExceptionWhenInvalidFormat() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/octet-stream",
                new ByteArrayInputStream("test content".getBytes())
        );

        doThrow(new InvalidFileFormatException("exe"))
                .when(fileValidator).validate(anyString(), anyLong(), anyLong());

        // Act & Assert
        assertThrows(InvalidFileFormatException.class, () ->
                uploadResourceUseCase.execute(file, "Test Document", "Desc", categoryId, "GUIDE"));
    }

    @Test
    void shouldThrowExceptionWhenCategoryNotFound() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new ByteArrayInputStream("test".getBytes())
        );

        UUID nonExistentCategoryId = UUID.randomUUID();
        when(resourceCategoryRepository.findById(nonExistentCategoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                uploadResourceUseCase.execute(file, "Test Document", "Desc", nonExistentCategoryId, "GUIDE"));
    }

    @Test
    void shouldThrowExceptionWhenTitleTooShort() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new ByteArrayInputStream("test".getBytes())
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                uploadResourceUseCase.execute(file, "Hi", "Desc", categoryId, "GUIDE"));
    }

    @Test
    void shouldThrowExceptionWhenTitleTooLong() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new ByteArrayInputStream("test".getBytes())
        );

        String longTitle = "A".repeat(201);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                uploadResourceUseCase.execute(file, longTitle, "Desc", categoryId, "GUIDE"));
    }

    @Test
    void shouldThrowExceptionWhenInvalidType() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                new ByteArrayInputStream("test".getBytes())
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                uploadResourceUseCase.execute(file, "Test Document", "Desc", categoryId, "INVALID_TYPE"));
    }

    @Test
    void shouldThrowExceptionWhenFileEmpty() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                uploadResourceUseCase.execute(file, "Test Document", "Desc", categoryId, "GUIDE"));
    }
}
