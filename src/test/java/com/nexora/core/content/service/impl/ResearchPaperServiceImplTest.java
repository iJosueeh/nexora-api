package com.nexora.core.content.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexora.core.application.content.usecases.papers.commands.SavePaperUseCase;
import com.nexora.core.application.content.usecases.papers.commands.IncrementPaperViewsUseCase;
import com.nexora.core.application.content.usecases.papers.queries.GetPaperBySlugUseCase;
import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.domain.content.repositories.PaperRepository;

@ExtendWith(MockitoExtension.class)
class ResearchPaperServiceImplTest {

    @Mock
    private PaperRepository paperRepository;

    @Mock
    private GetPaperBySlugUseCase getPaperBySlugUseCase;

    @Mock
    private SavePaperUseCase savePaperUseCase;

    @InjectMocks
    private IncrementPaperViewsUseCase incrementPaperViewsUseCase;

    @Test
    void save_GeneratesSlug() {
        SavePaperUseCase realSaveUseCase = new SavePaperUseCase(paperRepository);
        ResearchPaper paper = ResearchPaper.create(null, "Optimización de Algoritmos en IA",
                "Summary", "Facultad", UUID.randomUUID(), null);

        when(paperRepository.save(any(ResearchPaper.class))).thenAnswer(i -> i.getArguments()[0]);

        ResearchPaper savedPaper = realSaveUseCase.execute(paper);

        assertNotNull(savedPaper.getSlug());
        assertEquals("optimizacion-de-algoritmos-en-ia", savedPaper.getSlug());
        verify(paperRepository, times(1)).save(paper);
    }

    @Test
    void incrementViews_Success() {
        String slug = "test-slug";
        UUID authorId = UUID.randomUUID();
        ResearchPaper paper = ResearchPaper.create(slug, "Title", "Summary", "Facultad", authorId, null);
        paper.setViews(10);

        when(getPaperBySlugUseCase.execute(slug)).thenReturn(Optional.of(paper));

        incrementPaperViewsUseCase.execute(slug);

        assertEquals(11, paper.getViews());
        verify(getPaperBySlugUseCase, times(1)).execute(slug);
        verify(savePaperUseCase, times(1)).execute(paper);
    }
}
