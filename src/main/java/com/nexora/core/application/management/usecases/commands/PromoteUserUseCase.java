package com.nexora.core.application.management.usecases.commands;

import com.nexora.core.application.auth.dto.ProfileView;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PromoteUserUseCase {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public ProfileView execute(UUID userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));
        user.changeRole(newRole);
        User savedUser = userRepository.save(user);
        return mapToProfileView(savedUser);
    }

    private ProfileView mapToProfileView(User user) {
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        return new ProfileView(
                user.getId(),
                user.getEmail().value(),
                profile != null ? profile.getUsername().value() : null,
                profile != null && profile.getFullName() != null ? profile.getFullName().value() : "Sin nombre",
                profile != null && profile.getBio() != null ? profile.getBio().value() : null,
                (profile != null && profile.getCareer() != null) ? profile.getCareer().name() : null,
                profile != null ? profile.getAvatarUrl() : null,
                profile != null ? profile.getBannerUrl() : null,
                profile != null ? profile.getFollowersCount() : 0,
                profile != null ? profile.getFollowingCount() : 0,
                new ArrayList<>(),
                user.getIsActive(),
                false
        );
    }
}
