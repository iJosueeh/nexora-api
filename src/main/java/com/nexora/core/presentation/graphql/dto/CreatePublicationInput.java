package com.nexora.core.presentation.graphql.dto;

import java.util.List;

public record CreatePublicationInput(
        String titulo,
        String contenido,
        List<String> tags,
        String location,
        String imageUrl) {
}
