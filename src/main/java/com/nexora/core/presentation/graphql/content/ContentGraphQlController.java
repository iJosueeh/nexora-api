package com.nexora.core.presentation.graphql.content;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import com.nexora.core.domain.content.aggregates.ResearchPaper;
import com.nexora.core.domain.content.aggregates.UniversityEvent;
import com.nexora.core.domain.user.aggregates.Profile;
import com.nexora.core.application.content.usecases.feed.queries.SearchPostsUseCase;
import com.nexora.core.application.content.usecases.papers.queries.GetPapersUseCase;
import com.nexora.core.application.content.usecases.papers.queries.GetPaperBySlugUseCase;
import com.nexora.core.application.content.usecases.papers.queries.SearchPapersUseCase;
import com.nexora.core.application.content.usecases.papers.commands.IncrementPaperViewsUseCase;
import com.nexora.core.application.content.usecases.papers.commands.CreatePaperUseCase;
import com.nexora.core.application.content.usecases.papers.commands.EditPaperUseCase;
import com.nexora.core.application.content.usecases.papers.commands.DeletePaperUseCase;
import com.nexora.core.application.content.usecases.events.queries.GetEventsUseCase;
import com.nexora.core.application.content.usecases.events.queries.SearchEventsUseCase;
import com.nexora.core.application.content.usecases.events.commands.ConfirmRSVPUseCase;
import com.nexora.core.application.content.usecases.events.commands.CreateEventUseCase;
import com.nexora.core.application.content.usecases.events.commands.EditEventUseCase;
import com.nexora.core.application.content.usecases.events.commands.DeleteEventUseCase;
import com.nexora.core.application.content.usecases.events.queries.CheckBatchRegistrationUseCase;
import com.nexora.core.domain.user.repositories.ProfileRepository;
import com.nexora.core.application.content.dto.FeedAuthorView;
import com.nexora.core.application.security.services.SecurityService;
import com.nexora.core.presentation.graphql.dto.CreateEventInput;
import com.nexora.core.presentation.graphql.dto.UpdateEventInput;
import com.nexora.core.presentation.graphql.dto.CreateResearchPaperInput;
import com.nexora.core.presentation.graphql.dto.UpdateResearchPaperInput;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ContentGraphQlController {

    private final GetPapersUseCase getPapersUseCase;
    private final GetPaperBySlugUseCase getPaperBySlugUseCase;
    private final IncrementPaperViewsUseCase incrementPaperViewsUseCase;
    private final CreatePaperUseCase createPaperUseCase;
    private final EditPaperUseCase editPaperUseCase;
    private final DeletePaperUseCase deletePaperUseCase;
    private final SearchPostsUseCase searchPostsUseCase;
    private final SearchEventsUseCase searchEventsUseCase;
    private final SearchPapersUseCase searchPapersUseCase;
    private final GetEventsUseCase getEventsUseCase;
    private final ConfirmRSVPUseCase confirmRSVPUseCase;
    private final CreateEventUseCase createEventUseCase;
    private final EditEventUseCase editEventUseCase;
    private final DeleteEventUseCase deleteEventUseCase;
    private final CheckBatchRegistrationUseCase checkBatchRegistrationUseCase;
    private final SecurityService securityService;
    private final ProfileRepository profileRepository;

    @QueryMapping
    public List<ResearchPaper> researchPapers(@Argument int limit, @Argument int offset, @Argument String faculty) {
        return getPapersUseCase.execute(limit, offset, faculty);
    }

    @QueryMapping
    public ResearchPaper researchBySlug(@Argument String slug) {
        ResearchPaper paper = getPaperBySlugUseCase.execute(slug)
                .orElseThrow(() -> new RuntimeException("Investigación no encontrada"));
        incrementPaperViewsUseCase.execute(slug);
        return paper;
    }

    @QueryMapping
    public List<UniversityEvent> universityEvents(@Argument int limit, @Argument int offset, @Argument String category) {
        return getEventsUseCase.findAll(limit, offset, category);
    }

    @QueryMapping
    public UniversityEvent eventBySlug(@Argument String slug) {
        return getEventsUseCase.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }

    @QueryMapping
    public UniversityEvent eventById(@Argument UUID id) {
        return getEventsUseCase.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));
    }

    @QueryMapping
    public List<com.nexora.core.application.content.dto.FeedPostView> searchPosts(@Argument String query, @Argument int limit, @Argument int offset) {
        List<com.nexora.core.domain.content.aggregates.Post> posts = searchPostsUseCase.execute(query, limit, offset);
        UUID currentUserId = null;
        try {
            currentUserId = securityService.getCurrentUserId();
        } catch (Exception ignored) {}

        List<UUID> authorIds = posts.stream().map(p -> p.getAutor().getId()).distinct().toList();
        java.util.Map<UUID, Profile> profileMap = profileRepository.findByUserIdIn(authorIds).stream()
                .collect(java.util.stream.Collectors.toMap(Profile::getUserId, p -> p));

        return posts.stream().map(post -> {
            Profile profile = profileMap.get(post.getAutor().getId());
            FeedAuthorView autor = new FeedAuthorView(
                    post.getAutor().getId(),
                    profile != null && profile.getUsername() != null ? profile.getUsername().value() : null,
                    profile != null && profile.getFullName() != null ? profile.getFullName().value() : "Sin nombre",
                    profile != null ? profile.getAvatarUrl() : null
            );
            return new com.nexora.core.application.content.dto.FeedPostView(
                    post.getId(),
                    post.getTitulo(),
                    post.getContent(),
                    Boolean.TRUE.equals(post.getIsOfficial()),
                    post.getCreatedAt() != null ? post.getCreatedAt().atOffset(java.time.ZoneOffset.UTC) : null,
                    0, 0, false,
                    autor,
                    post.getTags() == null ? java.util.List.of() : java.util.List.copyOf(post.getTags()),
                    post.getLocation(),
                    post.getImageUrl()
            );
        }).toList();
    }

    @QueryMapping
    public List<UniversityEvent> searchEvents(@Argument String query, @Argument int limit, @Argument int offset) {
        return searchEventsUseCase.execute(query, limit, offset);
    }

    @QueryMapping
    public List<ResearchPaper> searchPapers(@Argument String query, @Argument int limit, @Argument int offset) {
        return searchPapersUseCase.execute(query, limit, offset);
    }

    @MutationMapping
    public UniversityEvent confirmRSVP(@Argument UUID eventId) {
        UUID userId = securityService.getCurrentUserId();
        return confirmRSVPUseCase.execute(eventId, userId);
    }

    @MutationMapping
    public UniversityEvent crearEvento(@Argument CreateEventInput input) {
        return createEventUseCase.execute(
                input.title(), input.description(), input.date(),
                input.location(), input.category(), input.image(),
                input.organizerName(), input.organizerRole(),
                input.whatsapp(), input.telegram(), input.discord());
    }

    @MutationMapping
    public UniversityEvent editarEvento(@Argument UUID eventId, @Argument UpdateEventInput input) {
        return editEventUseCase.execute(
                eventId, input.title(), input.description(), input.date(),
                input.location(), input.category(), input.image(),
                input.organizerName(), input.organizerRole(),
                input.whatsapp(), input.telegram(), input.discord());
    }

    @MutationMapping
    public boolean eliminarEvento(@Argument UUID eventId) {
        return deleteEventUseCase.execute(eventId);
    }

    @MutationMapping
    public ResearchPaper crearRecurso(@Argument CreateResearchPaperInput input) {
        UUID userId = securityService.getCurrentUserId();
        return createPaperUseCase.execute(
                input.title(), input.summary(), input.faculty(),
                userId, input.pdfUrl());
    }

    @MutationMapping
    public ResearchPaper editarRecurso(@Argument UUID paperId, @Argument UpdateResearchPaperInput input) {
        UUID userId = securityService.getCurrentUserId();
        return editPaperUseCase.execute(
                paperId, userId, input.title(), input.summary(),
                input.faculty(), input.pdfUrl());
    }

    @MutationMapping
    public boolean eliminarRecurso(@Argument UUID paperId) {
        UUID userId = securityService.getCurrentUserId();
        return deletePaperUseCase.execute(paperId, userId);
    }

    @BatchMapping(typeName = "UniversityEvent", field = "isUserRegistered")
    public Map<UniversityEvent, Boolean> isUserRegistered(List<UniversityEvent> events) {
        try {
            UUID userId = securityService.getCurrentUserId();
            List<UUID> eventIds = events.stream().map(UniversityEvent::getId).toList();
            Map<UUID, Boolean> registrations = checkBatchRegistrationUseCase.execute(eventIds, userId);
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
            UniversityEvent::getAttendeesCount
        ));
    }

    @BatchMapping(typeName = "ResearchPaper", field = "author")
    public Map<ResearchPaper, FeedAuthorView> author(List<ResearchPaper> papers) {
        List<UUID> authorIds = papers.stream().map(ResearchPaper::getAuthorId).distinct().toList();
        Map<UUID, FeedAuthorView> authorMap = profileRepository.findByUserIdIn(authorIds).stream()
                .collect(Collectors.toMap(
                    Profile::getUserId,
                    p -> new FeedAuthorView(p.getUserId(), p.getUsername().value(),
                        p.getFullName() != null ? p.getFullName().value() : null,
                        p.getAvatarUrl())
                ));
        return papers.stream().collect(Collectors.toMap(
            paper -> paper,
            paper -> authorMap.get(paper.getAuthorId())
        ));
    }
}
