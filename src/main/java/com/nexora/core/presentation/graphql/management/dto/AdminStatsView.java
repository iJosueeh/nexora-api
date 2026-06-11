package com.nexora.core.presentation.graphql.management.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsView {
    private int totalUsers;
    private int totalPosts;
    private int activeEvents;
    private List<RecentActivityView> recentActivity;
}
