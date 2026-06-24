package com.nexora.core.application.management.usecases.queries;

import com.nexora.core.application.auth.dto.ProfileView;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetAllUsersUseCase {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public List<ProfileView> execute(int limit, int offset, String search) {
        PageRequest pageRequest = PageRequest.of(offset / limit, limit);

        List<User> users;
        if (search != null && !search.isEmpty()) {
            users = userRepository.findByEmailContainingIgnoreCase(search, pageRequest).getContent();
        } else {
            users = userRepository.findAll(pageRequest).getContent();
        }

        return users.stream()
                .map(this::mapToProfileView)
                .collect(Collectors.toList());
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
