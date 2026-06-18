package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.StudyGroup;
import com.nexora.core.domain.content.repositories.StudyGroupRepository;
import com.nexora.core.infrastructure.persistence.content.entities.StudyGroupJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.StudyGroupMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.StudyGroupJpaRepository;
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
public class StudyGroupPersistenceAdapter implements StudyGroupRepository {

    private final StudyGroupJpaRepository studyGroupJpaRepository;
    private final StudyGroupMapper studyGroupMapper;
    private final UserJpaRepository userJpaRepository;

    @Override
    public List<StudyGroup> findAll(int limit, int offset, String category) {
        int page = offset / Math.max(limit, 1);
        PageRequest pageRequest = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("Todos")) {
            return studyGroupJpaRepository.findByCategoryIgnoreCase(category, pageRequest).stream()
                    .map(studyGroupMapper::toDomain)
                    .toList();
        }

        return studyGroupJpaRepository.findAll(pageRequest).stream()
                .map(studyGroupMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<StudyGroup> findBySlug(String slug) {
        return studyGroupJpaRepository.findBySlug(slug).map(studyGroupMapper::toDomain);
    }

    @Override
    public Optional<StudyGroup> findById(UUID id) {
        return studyGroupJpaRepository.findById(id).map(studyGroupMapper::toDomain);
    }

    @Override
    @Transactional
    public StudyGroup save(StudyGroup group) {
        UserJpaEntity author = null;
        if (group.getAuthorId() != null) {
            author = userJpaRepository.findById(group.getAuthorId()).orElse(null);
        }

        StudyGroupJpaEntity entity = studyGroupMapper.toJpa(group, author);
        StudyGroupJpaEntity saved = studyGroupJpaRepository.save(entity);
        return studyGroupMapper.toDomain(saved);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        studyGroupJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return studyGroupJpaRepository.findBySlug(slug).isPresent();
    }

    @Override
    public long count() {
        return studyGroupJpaRepository.count();
    }

    @Override
    public List<StudyGroup> findByAuthorId(UUID authorId) {
        return studyGroupJpaRepository.findByAuthorId(authorId).stream()
                .map(studyGroupMapper::toDomain)
                .toList();
    }

    @Override
    public List<StudyGroup> findByMemberId(UUID memberId) {
        return studyGroupJpaRepository.findByMemberId(memberId).stream()
                .map(studyGroupMapper::toDomain)
                .toList();
    }
}
