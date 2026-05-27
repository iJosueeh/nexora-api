package com.nexora.core.content.graphql;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import com.nexora.core.content.entity.ResearchPaper;
import com.nexora.core.content.entity.UniversityEvent;
import com.nexora.core.content.service.ResearchPaperService;
import com.nexora.core.content.service.UniversityEventService;
import com.nexora.core.security.service.SecurityService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ContentGraphQlController {

    private final ResearchPaperService researchService;
    private final UniversityEventService eventService;
    private final SecurityService securityService;

    @QueryMapping
    public List<ResearchPaper> researchPapers(@Argument int limit, @Argument int offset, @Argument String faculty) {
        return researchService.findAll(limit, offset, faculty);
    }

    @QueryMapping
    public ResearchPaper researchBySlug(@Argument String slug) {
        ResearchPaper paper = researchService.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Investigación no encontrada"));
        researchService.incrementViews(slug);
        return paper;
    }

    @QueryMapping
    public List<UniversityEvent> universityEvents(@Argument int limit, @Argument int offset, @Argument String category) {
        return eventService.findAll(limit, offset, category);
    }

    @QueryMapping
    public UniversityEvent eventBySlug(@Argument String slug) {
        return eventService.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }

    @MutationMapping
    public UniversityEvent confirmRSVP(@Argument UUID eventId) {
        UUID userId = securityService.getCurrentUserId();
        return eventService.confirmRSVP(eventId, userId);
    }

    @BatchMapping(typeName = "UniversityEvent", field = "isUserRegistered")
    public Map<UniversityEvent, Boolean> isUserRegistered(List<UniversityEvent> events) {
        try {
            UUID userId = securityService.getCurrentUserId();
            List<UUID> eventIds = events.stream().map(UniversityEvent::getId).toList();
            Map<UUID, Boolean> registrations = eventService.isUserRegisteredBatch(eventIds, userId);
            return events.stream().collect(Collectors.toMap(
                event -> event,
                event -> registrations.getOrDefault(event.getId(), false)
            ));
        } catch (Exception e) {
            return events.stream().collect(Collectors.toMap(event -> event, event -> false));
        }
    }

    @BatchMapping(typeName = "UniversityEvent", field = "attendeesCount")
    public Map<UniversityEvent, Integer> attendeesCount(List<UniversityEvent> events) {
        return events.stream().collect(Collectors.toMap(
            event -> event,
            event -> event.getAttendees() != null ? event.getAttendees().size() : 0
        ));
    }
}
