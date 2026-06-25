package com.nexora.core.domain.content.aggregates;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class ResourceRating {
    private final UUID id;
    private final UUID resourceId;
    private final UUID userId;
    private final Integer rating;
}