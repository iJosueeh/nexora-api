package com.nexora.core.application.content.usecases.papers.commands;

import com.nexora.core.common.util.SlugUtils;
import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.domain.content.repositories.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CreatePaperUseCase {

    private final PaperRepository paperRepository;

    public ResearchPaper execute(String title, String summary, String faculty, UUID authorId, String pdfUrl) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título es obligatorio");
        }
        if (authorId == null) {
            throw new IllegalArgumentException("El autor es obligatorio");
        }

        String slug = SlugUtils.makeSlug(title);
        ResearchPaper paper = ResearchPaper.create(null, title, summary, faculty, authorId, pdfUrl);
        paper.setSlug(slug);

        return paperRepository.save(paper);
    }
}
