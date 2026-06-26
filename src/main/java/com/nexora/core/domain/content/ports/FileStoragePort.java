package com.nexora.core.domain.content.ports;

import java.io.InputStream;
import java.time.Duration;

public interface FileStoragePort {
    String upload(String bucket, String key, InputStream inputStream, String contentType, long maxSize);
    String generatePresignedDownloadUrl(String bucket, String key, Duration expiry);
}
