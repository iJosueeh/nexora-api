package com.nexora.core.application.management.usecases.queries;

import com.nexora.core.application.management.dto.AdminStatsView;
import com.nexora.core.application.management.dto.RecentActivityView;
import com.nexora.core.domain.content.repositories.EventRepository;
import com.nexora.core.domain.content.repositories.PostRepository;
import com.nexora.core.domain.user.repositories.UserRepository;
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

        return AdminStatsView.builder()
                .totalUsers((int) totalUsers)
                .totalPosts((int) totalPosts)
                .activeEvents((int) activeEvents)
                .recentActivity(recentActivity)
                .build();
    }
}
