package com.nexora.core.application.content.usecases.papers.queries;

import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.domain.content.repositories.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchPapersUseCase {

    private final PaperRepository paperRepository;

    public List<ResearchPaper> execute(String query, int limit, int offset) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return paperRepository.searchByFullText(query.trim(), limit, offset);
    }
}
