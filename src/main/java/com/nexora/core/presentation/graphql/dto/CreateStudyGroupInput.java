package com.nexora.core.presentation.graphql.dto;

public record CreateStudyGroupInput(
        String name,
        String description,
        String category,
        Boolean isPrivate,
        Integer maxMembers) {
}
