package com.nexora.core.domain.content.aggregates;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class ResourceCategory {
    private final UUID id;
    private final UUID careerId;
    private final String name;
}