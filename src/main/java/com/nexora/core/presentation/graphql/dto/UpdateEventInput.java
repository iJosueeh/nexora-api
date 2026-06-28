package com.nexora.core.presentation.graphql.dto;

import java.time.OffsetDateTime;

public record UpdateEventInput(
        String title,
        String description,
        OffsetDateTime date,
        String location,
        String category,
        String image,
        String organizerName,
        String organizerRole,
        String whatsapp,
        String telegram,
        String discord) {
}
