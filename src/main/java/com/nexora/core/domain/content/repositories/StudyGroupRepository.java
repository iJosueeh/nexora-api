package com.nexora.core.domain.content.repositories;

import com.nexora.core.domain.content.aggregates.StudyGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyGroupRepository {
    List<StudyGroup> findAll(int limit, int offset, String category);
    Optional<StudyGroup> findBySlug(String slug);
    Optional<StudyGroup> findById(UUID id);
    StudyGroup save(StudyGroup group);
    void deleteById(UUID id);
    boolean existsBySlug(String slug);
    long count();
    List<StudyGroup> findByAuthorId(UUID authorId);
    List<StudyGroup> findByMemberId(UUID memberId);
}
