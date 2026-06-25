package com.nexora.core.domain.content.ports;

import com.nexora.core.domain.content.aggregates.ResourceRating;
import java.util.Optional;
import java.util.UUID;

public interface ResourceRatingRepository {
    ResourceRating save(ResourceRating rating);
    Optional<ResourceRating> findByUserIdAndResourceId(UUID userId, UUID resourceId);
}
