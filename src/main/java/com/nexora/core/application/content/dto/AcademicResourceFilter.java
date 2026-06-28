package com.nexora.core.application.content.dto;

import java.util.UUID;

public record AcademicResourceFilter(
    UUID careerId,
    UUID categoryId,
    String type,
    UUID authorId,
    Double minRating,
    String query
) {}
