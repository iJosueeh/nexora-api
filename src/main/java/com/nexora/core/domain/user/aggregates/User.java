package com.nexora.core.domain.user.aggregates;

import com.nexora.core.domain.shared.model.DomainModel;
import com.nexora.core.domain.user.valueobjects.Email;
import com.nexora.core.domain.user.valueobjects.UserRole;
import com.nexora.core.domain.user.valueobjects.SupabaseId;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends DomainModel {
    private Email email;
    private UserRole role;
    private Boolean isActive;
    private SupabaseId supabaseId;

    public static User create(Email email, UserRole role, SupabaseId supabaseId) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setSupabaseId(supabaseId);
        user.setIsActive(true);
        return user;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void changeRole(UserRole newRole) {
        if (newRole == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.role = newRole;
    }

    public void updateSupabaseId(SupabaseId supabaseId) {
        this.supabaseId = supabaseId;
    }

    public boolean isAdmin() {
        return this.role == UserRole.ROLE_ADMIN;
    }

    public boolean isOfficial() {
        return this.role == UserRole.ROLE_OFFICIAL;
    }

    public boolean isStudent() {
        return this.role == UserRole.ROLE_STUDENT;
    }
}
