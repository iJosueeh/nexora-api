package com.nexora.core.user.services;

import com.nexora.core.common.exception.ResourceNotFoundException;
import com.nexora.core.user.dto.UserResponse;
import com.nexora.core.user.entity.User;
import com.nexora.core.user.enums.Role;
import com.nexora.core.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user-related operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  /**
   * Retrieves a user by their ID.
   *
   * @param id the UUID of the user.
   * @return the UserResponse DTO.
   */
  @Transactional(readOnly = true)
  public UserResponse getUserById(UUID id) {
    log.info("Encontrando usuario con id: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("Usuario con id {} no encontrado", id);
          throw new ResourceNotFoundException("User not found");
        });
    log.info("Usuario encontrado: {}", user.getEmail());
    return mapToUserResponse(user);
  }

  private UserResponse mapToUserResponse(User body) {
    return UserResponse.builder()
        .id(body.getId())
        .username(body.getProfile() != null ? body.getProfile().getUsername() : body.getEmail())
        .email(body.getEmail())
        .isActive(body.getIsActive())
        .role(Role.valueOf(body.getRole().getName()))
        .build();
  }

}
