package com.nexora.core.presentation.graphql.dto;

public record CreateResearchPaperInput(
        String title,
        String summary,
        String faculty,
        String pdfUrl) {
}
