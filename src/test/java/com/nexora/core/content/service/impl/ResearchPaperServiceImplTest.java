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

import com.nexora.core.application.content.services.impl.ResearchPaperServiceImpl;
import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.domain.content.repositories.PaperRepository;

@ExtendWith(MockitoExtension.class)
class ResearchPaperServiceImplTest {

    @Mock
    private PaperRepository paperRepository;

    @InjectMocks
    private ResearchPaperServiceImpl researchService;

    @Test
    void save_GeneratesSlug() {
        ResearchPaper paper = ResearchPaper.create(null, "Optimización de Algoritmos en IA",
                "Summary", "Facultad", UUID.randomUUID(), null);

        when(paperRepository.save(any(ResearchPaper.class))).thenAnswer(i -> i.getArguments()[0]);

        ResearchPaper savedPaper = researchService.save(paper);

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

        when(paperRepository.findBySlug(slug)).thenReturn(Optional.of(paper));
        when(paperRepository.save(any(ResearchPaper.class))).thenReturn(paper);

        researchService.incrementViews(slug);

        assertEquals(11, paper.getViews());
        verify(paperRepository, times(1)).save(paper);
    }
}
