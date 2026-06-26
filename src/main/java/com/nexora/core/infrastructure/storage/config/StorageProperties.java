package com.nexora.core.infrastructure.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String endpoint;
    private String region = "us-east-1";
    private String accessKey;
    private String secretKey;
    private Map<String, String> buckets;
    private Duration presignedUrlExpiry = Duration.ofMinutes(15);
    private long maxResourceSize = 20971520L; // 20MB

    public String getBucketResources() {
        return buckets != null ? buckets.get("resources") : "nexora-resources";
    }
}
