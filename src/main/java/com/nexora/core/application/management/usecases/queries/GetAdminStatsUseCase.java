package com.nexora.core.application.management.usecases.queries;

import com.nexora.core.application.management.dto.AdminStatsView;
import com.nexora.core.application.management.dto.AdminStatsView.DistributionPoint;
import com.nexora.core.application.management.dto.AdminStatsView.GrowthPoint;
import com.nexora.core.application.management.dto.RecentActivityView;
import com.nexora.core.domain.content.repositories.EventRepository;
import com.nexora.core.domain.content.repositories.PostRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
import com.nexora.core.infrastructure.persistence.user.repositories.ProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GetAdminStatsUseCase {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final EventRepository eventRepository;
    private final ProfileJpaRepository profileJpaRepository;

    public AdminStatsView execute() {
        long totalUsers = 0;
        long totalPosts = 0;
        long activeEvents = 0;

        try { totalUsers = userRepository.count(); } catch (Exception e) { log.error("Error counting users: {}", e.getMessage()); }
        try { totalPosts = postRepository.count(); } catch (Exception e) { log.error("Error counting posts: {}", e.getMessage()); }
        try { activeEvents = eventRepository.count(); } catch (Exception e) { log.warn("University events table might be missing: {}", e.getMessage()); }

        List<RecentActivityView> recentActivity = new ArrayList<>();
        try {
            recentActivity = postRepository.findAllByOrderByCreatedAtDesc().stream()
                    .limit(5)
                    .map(post -> RecentActivityView.builder()
                        .id(post.getId())
                        .type("POST_CREATED")
                        .description("Nueva publicación: " + post.getTitulo())
                        .createdAt(post.getCreatedAt() != null ? post.getCreatedAt().atOffset(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC))
                        .build())
                    .collect(Collectors.toList());
        } catch (Exception e) { log.error("Error fetching recent activity: {}", e.getMessage()); }

        List<DistributionPoint> careerDistribution = buildCareerDistribution((int) totalUsers);
        List<GrowthPoint> userGrowth = buildUserGrowth((int) totalUsers);

        return AdminStatsView.builder()
                .totalUsers((int) totalUsers)
                .totalPosts((int) totalPosts)
                .activeEvents((int) activeEvents)
                .recentActivity(recentActivity)
                .userGrowth(userGrowth)
                .careerDistribution(careerDistribution)
                .build();
    }

    private List<DistributionPoint> buildCareerDistribution(int totalUsers) {
        try {
            List<Object[]> counts = profileJpaRepository.countProfilesByCareer();
            if (counts.isEmpty()) return List.of();

            return counts.stream()
                    .limit(5)
                    .map(row -> {
                        String name = (String) row[0];
                        long count = ((Number) row[1]).longValue();
                        double pct = totalUsers > 0 ? Math.round((count * 100.0 / totalUsers) * 10.0) / 10.0 : 0;
                        return DistributionPoint.builder()
                                .category(name)
                                .count((int) count)
                                .percentage(pct)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Career distribution unavailable: {}", e.getMessage());
            return List.of();
        }
    }

    private List<GrowthPoint> buildUserGrowth(int totalUsers) {
        return List.of(
                GrowthPoint.builder().label("Ahora").value(totalUsers).build()
        );
    }
}
