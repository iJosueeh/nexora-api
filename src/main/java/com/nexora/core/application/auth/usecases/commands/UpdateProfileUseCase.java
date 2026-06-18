package com.nexora.core.application.auth.usecases.commands;

import com.nexora.core.application.auth.dto.ProfileView;
import com.nexora.core.domain.content.repositories.FollowRepository;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.*;
import com.nexora.core.infrastructure.persistence.user.entities.CourseJpaEntity;
import com.nexora.core.infrastructure.persistence.user.repositories.CourseRepository;
import com.nexora.core.presentation.graphql.dto.UpdateProfileInput;
import com.nexora.core.application.security.services.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateProfileUseCase {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CourseRepository courseRepository;
    private final FollowRepository followRepository;
    private final SecurityService securityService;

    public ProfileView execute(String email, UpdateProfileInput input) {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Profile profile = profileRepository.findByUserId(user.getId())
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

        Profile saved = profileRepository.save(profile);
        return buildProfileView(user, saved);
    }

    private ProfileView buildProfileView(User user, Profile profile) {
        UUID currentUserId = null;
        try {
            currentUserId = securityService.getCurrentUserId();
        } catch (Exception ignored) {}

        syncSocialCounters(user, profile);

        boolean isFollowing = false;
        if (currentUserId != null && !currentUserId.equals(user.getId())) {
            User viewer = userRepository.findById(currentUserId).orElse(null);
            if (viewer != null) {
                isFollowing = followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), user.getId());
            }
        }

        return new ProfileView(
                user.getId(),
                user.getEmail().value(),
                profile.getUsername().value(),
                profile.getFullName() != null ? profile.getFullName().value() : null,
                profile.getBio() != null ? profile.getBio().value() : null,
                profile.getCareer() != null ? profile.getCareer().name() : null,
                profile.getAvatarUrl(),
                profile.getBannerUrl(),
                profile.getFollowersCount(),
                profile.getFollowingCount(),
                profile.getAcademicInterests(),
                isProfileComplete(profile),
                isFollowing
        );
    }

    private void syncSocialCounters(User user, Profile profile) {
        long actualFollowers = followRepository.countByFollowingId(user.getId());
        long actualFollowing = followRepository.countByFollowerId(user.getId());

        boolean needsUpdate = false;
        if (profile.getFollowersCount() != (int) actualFollowers) {
            profile.setFollowersCount((int) actualFollowers);
            needsUpdate = true;
        }
        if (profile.getFollowingCount() != (int) actualFollowing) {
            profile.setFollowingCount((int) actualFollowing);
            needsUpdate = true;
        }

        if (needsUpdate) {
            profileRepository.save(profile);
        }
    }

    private boolean isProfileComplete(Profile profile) {
        if (profile == null) return false;
        return profile.getUsername() != null
                && profile.getUsername().value() != null && !profile.getUsername().value().isBlank()
                && profile.getFullName() != null
                && profile.getCareer() != null
                && profile.getAcademicInterests() != null && !profile.getAcademicInterests().isEmpty();
    }
}
