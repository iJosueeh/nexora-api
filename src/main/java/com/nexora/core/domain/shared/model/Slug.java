package com.nexora.core.domain.shared.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.regex.Pattern;

@Getter
@EqualsAndHashCode
public class Slug {
    private final String value;
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9-]");
    private static final Pattern DOUBLE_DASHES = Pattern.compile("-+");

    public Slug(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Slug source cannot be empty");
        }
        this.value = generateSlug(source);
    }

    private String generateSlug(String input) {
        String base = input.toLowerCase().trim();
        base = NON_ALPHANUMERIC.matcher(base).replaceAll("-");
        base = DOUBLE_DASHES.matcher(base).replaceAll("-");
        if (base.startsWith("-")) base = base.substring(1);
        if (base.endsWith("-")) base = base.substring(0, base.length() - 1);
        return base;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Slug of(String value) {
        return new Slug(value);
    }
}
