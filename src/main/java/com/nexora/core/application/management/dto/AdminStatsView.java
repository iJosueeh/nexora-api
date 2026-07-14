package com.nexora.core.application.management.dto;

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
    @Builder.Default
    private List<GrowthPoint> userGrowth = List.of();
    @Builder.Default
    private List<DistributionPoint> careerDistribution = List.of();

    @Data
    @Builder
    public static class GrowthPoint {
        private String label;
        private int value;
    }

    @Data
    @Builder
    public static class DistributionPoint {
        private String category;
        private int count;
        private double percentage;
    }
}
