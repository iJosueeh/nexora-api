package com.nexora.core.application.content.usecases.papers.commands;

import com.nexora.core.common.util.SlugUtils;
import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.domain.content.repositories.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SavePaperUseCase {

    private final PaperRepository paperRepository;

    public ResearchPaper execute(ResearchPaper paper) {
        if (paper.getSlug() == null || paper.getSlug().isEmpty()) {
            paper.setSlug(SlugUtils.makeSlug(paper.getTitle()));
        }
        return paperRepository.save(paper);
    }
}
