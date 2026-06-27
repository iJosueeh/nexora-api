package com.nexora.core.infrastructure.storage.adapters;

import com.nexora.core.domain.content.ports.FileStoragePort;
import com.nexora.core.infrastructure.storage.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SupabaseStorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final StorageProperties storageProperties;

    @Override
    public String upload(String bucket, String key, InputStream inputStream, String contentType, long maxSize) {
        try {
            byte[] bytes = inputStream.readAllBytes();

            if (bytes.length > maxSize) {
                throw new com.nexora.core.domain.content.exceptions.FileTooLargeException(maxSize);
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

            return key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file content", e);
        }
    }

    @Override
    public String generatePresignedDownloadUrl(String bucket, String key, Duration expiry) {
        // Supabase Storage public URL format (bucket is public)
        String baseEndpoint = storageProperties.getEndpoint()
                .replace("/storage/v1/s3", "/storage/v1/object/public");
        return baseEndpoint + "/" + bucket + "/" + key;
    }

    public String generateResourceKey(UUID authorId, String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        return authorId + "/" + UUID.randomUUID() + "." + extension;
    }
}
