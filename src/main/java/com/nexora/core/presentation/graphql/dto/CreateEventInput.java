package com.nexora.core.presentation.graphql.dto;

import java.util.List;

public record CreateEventInput(
        String title,
        String description,
        String date,
        String location,
        String category,
        String image,
        String organizerName,
        String organizerRole,
        String whatsapp,
        String telegram,
        String discord) {
}
