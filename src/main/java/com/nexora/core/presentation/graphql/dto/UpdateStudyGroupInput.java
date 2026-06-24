package com.nexora.core.presentation.graphql.dto;

public record UpdateStudyGroupInput(
        String name,
        String description,
        String category,
        Boolean isPrivate,
        Integer maxMembers) {
}
