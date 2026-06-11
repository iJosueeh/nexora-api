package com.nexora.core.presentation.graphql.dto;

public record TagSuggestionView(
        String id,
        String name,
        int usageCount) {
}
