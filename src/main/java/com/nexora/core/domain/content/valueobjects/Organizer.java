package com.nexora.core.domain.content.valueobjects;

import lombok.Getter;

@Getter
public class Organizer {
    private final String name;
    private final String role;

    private Organizer(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public static Organizer of(String name, String role) {
        return new Organizer(name, role);
    }
}
