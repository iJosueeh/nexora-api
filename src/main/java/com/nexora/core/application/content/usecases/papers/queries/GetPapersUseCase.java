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
public class GetPapersUseCase {

    private final PaperRepository paperRepository;

    public List<ResearchPaper> execute(int limit, int offset, String faculty) {
        return paperRepository.findAll(limit, offset, faculty);
    }
}
