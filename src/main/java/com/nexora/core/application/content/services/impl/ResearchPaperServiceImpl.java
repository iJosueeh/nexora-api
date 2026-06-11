package com.nexora.core.application.content.services.impl;

import com.nexora.core.application.content.services.ResearchPaperService;
import com.nexora.core.common.util.SlugUtils;
import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.domain.content.repositories.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResearchPaperServiceImpl implements ResearchPaperService {

    private final PaperRepository paperRepository;

    @Override
    public java.util.List<ResearchPaper> findAll(int limit, int offset, String faculty) {
        return paperRepository.findAll(limit, offset, faculty);
    }

    @Override
    public java.util.Optional<ResearchPaper> findBySlug(String slug) {
        return paperRepository.findBySlug(slug);
    }

    @Override
    @Transactional
    public ResearchPaper save(ResearchPaper paper) {
        if (paper.getSlug() == null || paper.getSlug().isEmpty()) {
            paper.setSlug(SlugUtils.makeSlug(paper.getTitle()));
        }
        return paperRepository.save(paper);
    }

    @Override
    @Transactional
    public void incrementViews(String slug) {
        paperRepository.findBySlug(slug).ifPresent(paper -> {
            paper.incrementViews();
            paperRepository.save(paper);
        });
    }
}
