package com.nexora.core.application.content.usecases.studygroups.queries;

import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscoverUsersUseCase {

    private static final int MAX_RESULTS = 10;

    private final ProfileRepository profileRepository;

    public List<UserSearchResultView> execute(UUID currentUserId, List<UUID> excludeUserIds) {
        List<UUID> allExcluded = new java.util.ArrayList<>(excludeUserIds);
        if (currentUserId != null && !allExcluded.contains(currentUserId)) {
            allExcluded.add(currentUserId);
        }

        if (allExcluded.isEmpty()) {
            allExcluded.add(UUID.randomUUID()); // dummy — ensures query doesn't fail on empty list
        }

        return profileRepository.findDiscoverableByUserIdNotIn(allExcluded, MAX_RESULTS).stream()
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
