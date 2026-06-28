package com.nexora.core.domain.content.aggregates;

import com.nexora.core.domain.content.enums.GroupMembershipStatus;
import com.nexora.core.domain.content.enums.GroupRole;
import com.nexora.core.domain.shared.model.DomainModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StudyGroup extends DomainModel {
    private String slug;
    private String name;
    private String description;
    private String category;
    private boolean isPrivate;
    private int maxMembers;
    private UUID authorId;
    private List<UUID> memberIds;
    private List<GroupMembershipInfo> memberships;
    private Boolean currentUserIsMember;
    private GroupRole currentUserRole;

    public static StudyGroup create(String slug, String name, String description,
                                     String category, boolean isPrivate, int maxMembers, UUID authorId) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("El slug del grupo es obligatorio");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del grupo es obligatorio");
        }
        if (name.length() < 3 || name.length() > 100) {
            throw new IllegalArgumentException("El nombre debe tener entre 3 y 100 caracteres");
        }

        StudyGroup group = new StudyGroup();
        group.setSlug(slug.trim());
        group.setName(name.trim());
        group.setDescription(description);
        group.setCategory(category != null ? category : "General");
        group.setPrivate(isPrivate);
        group.setMaxMembers(maxMembers > 0 ? maxMembers : 50);
        group.setAuthorId(authorId);
        group.setMemberIds(new ArrayList<>(List.of(authorId)));
        group.setMemberships(new ArrayList<>());
        group.setCurrentUserIsMember(false);
        group.setCurrentUserRole(null);
        return group;
    }

    public boolean isGroupFull() {
        return memberIds != null && memberIds.size() >= maxMembers;
    }

    public boolean isOwner(UUID userId) {
        return authorId != null && authorId.equals(userId);
    }

    public boolean hasMember(UUID userId) {
        return memberIds != null && memberIds.contains(userId);
    }

    public int memberCount() {
        return memberIds != null ? memberIds.size() : 0;
    }

    public void addMember(UUID userId) {
        if (memberIds == null) {
            memberIds = new ArrayList<>();
        }
        if (!memberIds.contains(userId)) {
            memberIds.add(userId);
        }
    }

    public void removeMember(UUID userId) {
        if (memberIds != null) {
            memberIds.remove(userId);
        }
    }

    public Boolean getIsMember() {
        return currentUserIsMember;
    }

    public GroupRole getMyRole() {
        return currentUserRole;
    }

    public record GroupMembershipInfo(UUID userId, GroupRole role, GroupMembershipStatus status) {}
}
