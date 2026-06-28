package com.nexora.core.domain.content.exceptions;

public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(long maxSize) {
        super("File exceeds maximum allowed size of " + maxSize + " bytes");
    }
}
