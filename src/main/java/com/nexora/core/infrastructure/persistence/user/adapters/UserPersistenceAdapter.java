package com.nexora.core.infrastructure.persistence.user.adapters;

import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.Email;
import com.nexora.core.infrastructure.persistence.user.entities.UserJpaEntity;
import com.nexora.core.infrastructure.persistence.user.mappers.UserMapper;
import com.nexora.core.infrastructure.persistence.user.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userJpaRepository.findByEmail(email.value()).map(userMapper::toDomain);
    }

    @Override
    public List<User> findAllById(List<UUID> ids) {
        return userJpaRepository.findAllById(ids).stream()
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userJpaRepository.findAll(pageable).map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = userMapper.toJpa(user);
        UserJpaEntity saved = userJpaRepository.save(entity);
        return userMapper.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.value());
    }

    @Override
    public Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable) {
        return userJpaRepository.findByEmailContainingIgnoreCase(email, pageable)
                .map(userMapper::toDomain);
    }

    @Override
    public long count() {
        return userJpaRepository.count();
    }
}
