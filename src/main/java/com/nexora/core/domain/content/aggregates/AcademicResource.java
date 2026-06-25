package com.nexora.core.domain.content.aggregates;

import lombok.Builder;
import lombok.Getter;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class AcademicResource {
    private final UUID id;
    private final String slug;
    private final String title;
    private final String description;
    private final String type;
    private final UUID categoryId;
    private final UUID authorId;
    private final String fileUrl;
    private final Long fileSize;
    private final String fileFormat;
    private final Double averageRating;
    private final Integer ratingsCount;
    private final Integer downloadCount;
    private final OffsetDateTime deletedAt;
}