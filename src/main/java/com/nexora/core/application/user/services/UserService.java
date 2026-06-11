package com.nexora.core.application.user.services;

import com.nexora.core.common.exception.ResourceNotFoundException;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.presentation.rest.user.dto.UserResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;

  @Transactional(readOnly = true)
  public UserResponse getUserById(UUID id) {
    log.info("Encontrando usuario con id: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Usuario con id {} no encontrado", id);
          throw new ResourceNotFoundException("User not found");
        });
    log.info("Usuario encontrado: {}", user.getEmail().value());
    return mapToUserResponse(user);
  }

  private UserResponse mapToUserResponse(User body) {
    Profile profile = profileRepository.findByUserId(body.getId()).orElse(null);
    return UserResponse.builder()
        .id(body.getId())
        .username(profile != null ? profile.getUsername().value() : body.getEmail().value())
        .email(body.getEmail().value())
        .isActive(body.getIsActive())
        .role(body.getRole().name())
        .build();
  }

}
