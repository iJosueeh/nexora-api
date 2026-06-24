package com.nexora.core.presentation.graphql.dto;

import java.util.List;

public record UpdatePublicationInput(
        String titulo,
        String contenido,
        List<String> tags,
        String location,
        String imageUrl) {
}
