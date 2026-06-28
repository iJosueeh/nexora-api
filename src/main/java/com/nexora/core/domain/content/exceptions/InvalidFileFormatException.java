package com.nexora.core.domain.content.exceptions;

public class InvalidFileFormatException extends RuntimeException {
    public InvalidFileFormatException(String format) {
        super("Invalid file format: " + format + ". Allowed formats: PDF, EPUB, MD, PPTX, DOCX");
    }
}
