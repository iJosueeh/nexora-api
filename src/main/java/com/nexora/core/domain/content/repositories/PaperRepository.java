package com.nexora.core.domain.content.repositories;

import com.nexora.core.domain.content.aggregates.ResearchPaper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaperRepository {
    List<ResearchPaper> findAll(int limit, int offset, String faculty);
    Optional<ResearchPaper> findBySlug(String slug);
    Optional<ResearchPaper> findById(UUID id);
    ResearchPaper save(ResearchPaper paper);
}
