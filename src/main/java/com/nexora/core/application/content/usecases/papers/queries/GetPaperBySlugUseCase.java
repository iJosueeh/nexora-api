package com.nexora.core.application.content.usecases.papers.queries;

import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.domain.content.repositories.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetPaperBySlugUseCase {

    private final PaperRepository paperRepository;

    public Optional<ResearchPaper> execute(String slug) {
        return paperRepository.findBySlug(slug);
    }
}
