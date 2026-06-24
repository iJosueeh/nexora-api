package com.nexora.core.presentation.graphql.dto;

public record UpdateResearchPaperInput(
        String title,
        String summary,
        String faculty,
        String pdfUrl) {
}
