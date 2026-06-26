package com.nexora.core.infrastructure.storage;

import com.nexora.core.domain.content.exceptions.FileTooLargeException;
import com.nexora.core.domain.content.exceptions.InvalidFileFormatException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FileValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "epub", "md", "pptx", "docx");

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/epub+zip",
            "text/markdown",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public void validate(String filename, long fileSize, long maxSize) {
        if (fileSize > maxSize) {
            throw new FileTooLargeException(maxSize);
        }

        String extension = extractExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileFormatException(extension);
        }
    }

    public void validateMimeType(String mimeType) {
        if (mimeType != null && !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new InvalidFileFormatException(mimeType);
        }
    }

    public String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
