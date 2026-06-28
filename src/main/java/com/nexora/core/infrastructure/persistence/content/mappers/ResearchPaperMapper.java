package com.nexora.core.infrastructure.persistence.content.mappers;

import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.infrastructure.persistence.content.entities.ResearchPaperJpaEntity;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ResearchPaperMapper {

    public ResearchPaper toDomain(ResearchPaperJpaEntity entity) {
        if (entity == null) return null;

        return ResearchPaper.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .title(entity.getTitle())
                .summary(entity.getSummary())
                .faculty(entity.getFaculty())
                .views(entity.getViews() != null ? entity.getViews() : 0)
                .authorId(entity.getAuthor() != null ? entity.getAuthor().getId() : null)
                .pdfUrl(entity.getPdfUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ResearchPaperJpaEntity toJpa(ResearchPaper domain, UserJpaEntity author) {
        if (domain == null) return null;

        ResearchPaperJpaEntity entity = ResearchPaperJpaEntity.builder()
                .slug(domain.getSlug())
                .title(domain.getTitle())
                .summary(domain.getSummary())
                .faculty(domain.getFaculty())
                .views(domain.getViews())
                .author(author)
                .pdfUrl(domain.getPdfUrl())
                .build();
        entity.setId(domain.getId());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
