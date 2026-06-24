package com.nexora.core.application.content.usecases.studygroups.queries;

import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchUsersUseCase {

    private static final int MAX_RESULTS = 10;

    private final ProfileRepository profileRepository;

    public List<UserSearchResultView> execute(String query) {
        if (query == null || query.trim().length() < 2) {
            return List.of();
        }

        return profileRepository.searchByUsername(query.trim(), MAX_RESULTS).stream()
                .map(this::toView)
                .toList();
    }

    private UserSearchResultView toView(Profile profile) {
        return new UserSearchResultView(
                profile.getUserId(),
                profile.getUsername().value(),
                profile.getFullName() != null ? profile.getFullName().value() : null,
                profile.getAvatarUrl()
        );
    }
}
