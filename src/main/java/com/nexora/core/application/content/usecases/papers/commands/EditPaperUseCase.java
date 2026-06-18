package com.nexora.core.application.content.usecases.papers.commands;

import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.domain.content.repositories.PaperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EditPaperUseCase {

    private final PaperRepository paperRepository;

    public ResearchPaper execute(UUID paperId, UUID currentUserId, String title, String summary, String faculty, String pdfUrl) {
        ResearchPaper paper = paperRepository.findById(paperId)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado"));

        if (!paper.getAuthorId().equals(currentUserId)) {
            throw new SecurityException("No tienes permiso para editar este recurso");
        }

        if (title != null && !title.isBlank()) {
            paper.setTitle(title.trim());
        }
        if (summary != null) {
            paper.setSummary(summary);
        }
        if (faculty != null && !faculty.isBlank()) {
            paper.setFaculty(faculty);
        }
        if (pdfUrl != null) {
            paper.setPdfUrl(pdfUrl);
        }

        return paperRepository.save(paper);
    }
}
