package com.nexora.core.application.content.services;

import com.nexora.core.domain.content.aggregates.ResearchPaper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResearchPaperService {
    List<ResearchPaper> findAll(int limit, int offset, String faculty);
    Optional<ResearchPaper> findBySlug(String slug);
    ResearchPaper save(ResearchPaper paper);
    void incrementViews(String slug);
}
