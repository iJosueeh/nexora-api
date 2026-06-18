package com.nexora.core.application.management.usecases.commands;

import com.nexora.core.application.auth.dto.ProfileView;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.*;
import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.CourseRepository;
import com.nexora.core.presentation.graphql.dto.UpdateProfileInput;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateProfileAdminUseCase {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CourseRepository courseRepository;

    public ProfileView execute(UUID targetUserId, UpdateProfileInput input) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (input.username() != null) profile.setUsername(new Username(input.username()));
        if (input.fullName() != null) profile.setFullName(new FullName(input.fullName()));
        if (input.bio() != null) profile.setBio(new Bio(input.bio()));
        if (input.avatarUrl() != null) profile.setAvatarUrl(input.avatarUrl());
        if (input.bannerUrl() != null) profile.setBannerUrl(input.bannerUrl());

        if (input.career() != null && !input.career().isBlank()) {
            CourseJpaEntity career = courseRepository.findByNameIgnoreCase(input.career().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Career not found: " + input.career()));
            profile.setCareer(new Career(career.getId(), career.getName(), career.getFacultad().getId()));
        }

        if (input.academicInterests() != null) {
            profile.setAcademicInterests(input.academicInterests());
        }

        profileRepository.save(profile);

        return new ProfileView(
                targetUser.getId(),
                targetUser.getEmail().value(),
                profile.getUsername().value(),
                profile.getFullName() != null ? profile.getFullName().value() : null,
                profile.getBio() != null ? profile.getBio().value() : null,
                profile.getCareer() != null ? profile.getCareer().name() : null,
                profile.getAvatarUrl(),
                profile.getBannerUrl(),
                profile.getFollowersCount(),
                profile.getFollowingCount(),
                profile.getAcademicInterests(),
                true,
                false
        );
    }
}
