package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.domain.content.repositories.PaperRepository;
import com.nexora.core.infrastructure.persistence.content.entities.ResearchPaperJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.ResearchPaperMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.ResearchPaperJpaRepository;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaperPersistenceAdapter implements PaperRepository {

    private final ResearchPaperJpaRepository paperJpaRepository;
    private final ResearchPaperMapper paperMapper;
    private final UserJpaRepository userJpaRepository;

    @Override
    public List<ResearchPaper> findAll(int limit, int offset, String faculty) {
        int page = offset / limit;
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        if (faculty != null && !faculty.equalsIgnoreCase("Todos")) {
            return paperJpaRepository.findByFacultyIgnoreCase(faculty, pageRequest).stream()
                    .map(paperMapper::toDomain)
                    .toList();
        }

        return paperJpaRepository.findAll(pageRequest).stream()
                .map(paperMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ResearchPaper> findBySlug(String slug) {
        return paperJpaRepository.findBySlug(slug).map(paperMapper::toDomain);
    }

    @Override
    public Optional<ResearchPaper> findById(UUID id) {
        return paperJpaRepository.findById(id).map(paperMapper::toDomain);
    }

    @Override
    @Transactional
    public ResearchPaper save(ResearchPaper paper) {
        UserJpaEntity author = userJpaRepository.findById(paper.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));

        ResearchPaperJpaEntity entity = paperMapper.toJpa(paper, author);
        ResearchPaperJpaEntity saved = paperJpaRepository.save(entity);
        return paperMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        paperJpaRepository.deleteById(id);
    }

    @Override
    public long count(String faculty) {
        if (faculty != null && !faculty.equalsIgnoreCase("Todos")) {
            return paperJpaRepository.countByFacultyIgnoreCase(faculty);
        }
        return paperJpaRepository.count();
    }
}
