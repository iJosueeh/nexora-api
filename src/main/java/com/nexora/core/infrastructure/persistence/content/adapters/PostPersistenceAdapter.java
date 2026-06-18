package com.nexora.core.infrastructure.persistence.content.adapters;

import com.nexora.core.domain.content.aggregates.Post;
import com.nexora.core.domain.content.repositories.PostRepository;
import com.nexora.core.infrastructure.persistence.content.entities.PostJpaEntity;
import com.nexora.core.infrastructure.persistence.content.mappers.PostMapper;
import com.nexora.core.infrastructure.persistence.content.repositories.PostJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostPersistenceAdapter implements PostRepository {

    private final PostJpaRepository postJpaRepository;
    private final PostMapper postMapper;

    @Override
    public List<Post> findAllByOrderByCreatedAtDesc() {
        return postJpaRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(postMapper::toDomain)
                .toList();
    }

    @Override
    public Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable) {
        return postJpaRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(postMapper::toDomain);
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return postJpaRepository.findById(id).map(postMapper::toDomain);
    }

    @Override
    public List<Post> findAllByIdIn(List<UUID> ids) {
        return postJpaRepository.findAllById(ids).stream()
                .map(postMapper::toDomain)
                .toList();
    }

    @Override
    public List<Post> searchByFullText(String query, int limit, int offset) {
        return postJpaRepository.searchByFullText(query, PageRequest.of(offset / limit, limit)).stream()
                .map(postMapper::toDomain)
                .toList();
    }

    @Override
    public Post save(Post post) {
        PostJpaEntity entity = postMapper.toJpa(post);
        PostJpaEntity saved = postJpaRepository.save(entity);
        return postMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        postJpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return postJpaRepository.count();
    }
}
