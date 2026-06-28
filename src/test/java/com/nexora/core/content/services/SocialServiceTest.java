package com.nexora.core.content.services;

import com.nexora.core.application.content.usecases.social.commands.ToggleFollowUseCase;
import com.nexora.core.application.content.usecases.social.queries.GetFollowersUseCase;
import com.nexora.core.application.content.usecases.social.queries.GetFollowingUseCase;
import com.nexora.core.domain.content.aggregates.Follow;
import com.nexora.core.domain.content.repositories.FollowRepository;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.domain.user.aggregates.User;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.domain.user.valueobjects.*;
import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.application.auth.dto.ProfileView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialServiceTest {

    @Mock
    private FollowRepository followRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityService securityService;

    @InjectMocks
    private ToggleFollowUseCase toggleFollowUseCase;
    @InjectMocks
    private GetFollowersUseCase getFollowersUseCase;
    @InjectMocks
    private GetFollowingUseCase getFollowingUseCase;

    private UUID currentUserId;
    private UUID targetUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        targetUserId = UUID.randomUUID();
    }

    @Test
    void toggleFollowShouldDeleteAndDecrementWhenAlreadyFollowing() {
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);
        when(followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)).thenReturn(true);

        boolean result = toggleFollowUseCase.execute(targetUserId);

        assertFalse(result);
        verify(followRepository).deleteByFollowerIdAndFollowingId(currentUserId, targetUserId);
        verify(profileRepository).decrementFollowingCount(currentUserId);
        verify(profileRepository).decrementFollowersCount(targetUserId);
    }

    @Test
    void toggleFollowShouldSaveAndIncrementWhenNotFollowing() {
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);
        when(followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId)).thenReturn(false);

        boolean result = toggleFollowUseCase.execute(targetUserId);

        assertTrue(result);
        verify(followRepository).save(any(Follow.class));
        verify(profileRepository).incrementFollowingCount(currentUserId);
        verify(profileRepository).incrementFollowersCount(targetUserId);
    }

    @Test
    void toggleFollowShouldThrowExceptionWhenFollowingSelf() {
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);

        assertThrows(IllegalArgumentException.class, () -> toggleFollowUseCase.execute(currentUserId));
        verifyNoInteractions(followRepository);
    }

    @Test
    void getFollowersShouldReturnBatchMappedProfileViews() {
        UUID userId = UUID.randomUUID();
        UUID followerUserId = UUID.randomUUID();

        Profile profile = Profile.builder()
                .userId(followerUserId)
                .username(new Username("follower_uname"))
                .fullName(new FullName("Follower Name"))
                .bio(new Bio("Bio test"))
                .followersCount(5)
                .followingCount(10)
                .build();

        User followerUser = User.builder()
                .id(followerUserId)
                .email(new Email("follower@test.com"))
                .build();

        when(followRepository.findFollowerIdsByFollowingId(userId)).thenReturn(List.of(followerUserId));
        when(profileRepository.findByUserIdIn(List.of(followerUserId))).thenReturn(List.of(profile));
        when(userRepository.findAllById(List.of(followerUserId))).thenReturn(List.of(followerUser));
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);
        when(followRepository.findFollowingIdsByFollowerIdAndFollowingIdsIn(currentUserId, List.of(followerUserId)))
                .thenReturn(List.of(followerUserId));

        List<ProfileView> result = getFollowersUseCase.execute(userId);

        assertEquals(1, result.size());
        ProfileView view = result.get(0);
        assertEquals(followerUserId, view.id());
        assertEquals("follower@test.com", view.email());
        assertEquals("follower_uname", view.username());
        assertEquals("Follower Name", view.fullName());
        assertEquals("Bio test", view.bio());
        assertEquals(5, view.followersCount());
        assertEquals(10, view.followingCount());
        assertTrue(view.isFollowing());
    }

    @Test
    void getFollowingShouldReturnBatchMappedProfileViews() {
        UUID userId = UUID.randomUUID();
        UUID followingUserId = UUID.randomUUID();

        Profile profile = Profile.builder()
                .userId(followingUserId)
                .username(new Username("following_uname"))
                .fullName(new FullName("Following Name"))
                .bio(new Bio("Bio test 2"))
                .followersCount(8)
                .followingCount(12)
                .build();

        User followingUser = User.builder()
                .id(followingUserId)
                .email(new Email("following@test.com"))
                .build();

        when(followRepository.findFollowingIdsByFollowerId(userId)).thenReturn(List.of(followingUserId));
        when(profileRepository.findByUserIdIn(List.of(followingUserId))).thenReturn(List.of(profile));
        when(userRepository.findAllById(List.of(followingUserId))).thenReturn(List.of(followingUser));
        when(securityService.getCurrentUserId()).thenReturn(currentUserId);
        when(followRepository.findFollowingIdsByFollowerIdAndFollowingIdsIn(currentUserId, List.of(followingUserId)))
                .thenReturn(List.of());

        List<ProfileView> result = getFollowingUseCase.execute(userId);

        assertEquals(1, result.size());
        ProfileView view = result.get(0);
        assertEquals(followingUserId, view.id());
        assertEquals("following@test.com", view.email());
        assertEquals("following_uname", view.username());
        assertEquals("Following Name", view.fullName());
        assertEquals("Bio test 2", view.bio());
        assertEquals(8, view.followersCount());
        assertEquals(12, view.followingCount());
        assertFalse(view.isFollowing());
    }
}
