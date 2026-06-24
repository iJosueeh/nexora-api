package com.nexora.core.domain.user.repositories;

import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.valueobjects.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(Email email);
    List<User> findAllById(List<UUID> ids);
    Page<User> findAll(Pageable pageable);
    User save(User user);
    void deleteById(UUID id);
    boolean existsByEmail(Email email);
    Page<User> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    long count();
}
