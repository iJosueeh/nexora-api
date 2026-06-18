package com.nexora.core.presentation.graphql.dto;

public record UpdateEventInput(
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
