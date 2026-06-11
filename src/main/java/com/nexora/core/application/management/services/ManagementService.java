package com.nexora.core.application.management.services;

import java.util.List;
import java.util.UUID;
import com.nexora.core.presentation.graphql.dto.ProfileView;
import com.nexora.core.presentation.graphql.management.dto.AdminStatsView;
import com.nexora.core.presentation.graphql.dto.FeedPostView;

public interface ManagementService {
    AdminStatsView getAdminStats();
    List<ProfileView> getAllUsers(int limit, int offset, String search);
    ProfileView updateUserStatus(UUID userId, boolean isActive);
    FeedPostView markPostAsOfficial(UUID postId, boolean isOfficial);
    boolean deletePost(UUID postId);
}
