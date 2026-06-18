package com.nexora.core.application.content.usecases.papers.commands;

import com.nexora.core.application.content.usecases.papers.queries.GetPaperBySlugUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class IncrementPaperViewsUseCase {

    private final GetPaperBySlugUseCase getPaperBySlugUseCase;
    private final SavePaperUseCase savePaperUseCase;

    public void execute(String slug) {
        getPaperBySlugUseCase.execute(slug).ifPresent(paper -> {
            paper.incrementViews();
            savePaperUseCase.execute(paper);
        });
    }
}
