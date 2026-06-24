package com.nexora.core.application.user.usecases.queries;

import com.nexora.core.common.exception.ResourceNotFoundException;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.presentation.rest.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserByIdUseCase {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public UserResponse execute(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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
