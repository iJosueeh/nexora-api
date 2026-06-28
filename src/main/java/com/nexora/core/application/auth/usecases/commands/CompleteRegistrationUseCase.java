package com.nexora.core.application.auth.usecases.commands;

import com.nexora.core.application.auth.dto.AuthResponse;
import com.nexora.core.application.auth.ports.CourseCatalogPort;
import com.nexora.core.application.auth.ports.CourseCatalogPort.CourseData;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.*;
import com.nexora.core.presentation.rest.auth.dto.RegisterUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompleteRegistrationUseCase {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CourseCatalogPort courseCatalogPort;
    private final ResolveSessionUseCase resolveSessionUseCase;

    public AuthResponse execute(String email, RegisterUpdateRequest request) {
        User user = userRepository.findByEmail(new Email(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found in local database"));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found for user"));

        if (request.getUsername() != null) profile.setUsername(new Username(request.getUsername()));
        if (request.getFullName() != null) profile.setFullName(new FullName(request.getFullName()));
        if (request.getBio() != null) profile.setBio(new Bio(request.getBio()));
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getBannerUrl() != null) profile.setBannerUrl(request.getBannerUrl());

        if (request.getCareer() != null && !request.getCareer().isBlank()) {
            CourseData career = courseCatalogPort.findByNameIgnoreCase(request.getCareer().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Career not found: " + request.getCareer()));
            profile.setCareer(new Career(career.id(), career.name(), career.facultyId()));
        }

        String[] incomingInterests = resolveIncomingInterests(request);
        if (incomingInterests.length > 0) {
            profile.setAcademicInterests(Arrays.asList(incomingInterests));
        }

        profileRepository.save(profile);

        return resolveSessionUseCase.execute(email, null);
    }

    private String[] resolveIncomingInterests(RegisterUpdateRequest request) {
        String[] source = request.getAcademicInterests();
        if (source == null || source.length == 0) {
            source = request.getSelectedInterests();
        }

        if (source == null || source.length == 0) {
            return new String[0];
        }

        return Arrays.stream(source)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toArray(String[]::new);
    }
}
