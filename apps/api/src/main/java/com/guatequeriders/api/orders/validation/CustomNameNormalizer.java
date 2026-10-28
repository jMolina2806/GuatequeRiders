package com.guatequeriders.api.orders.validation;

import java.util.regex.Pattern;

public final class CustomNameNormalizer {

    private static final int MAX_LENGTH = 20;
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile(
            "^[\\p{L}\\p{N}](?:[\\p{L}\\p{N} '\\-]*[\\p{L}\\p{N}])?$"
    );

    private CustomNameNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }
        if (value.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Custom name contains control characters");
        }

        String normalized = value.strip();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.codePointCount(0, normalized.length()) > MAX_LENGTH) {
            throw new IllegalArgumentException("Custom name must not exceed 20 characters");
        }
        if (!ALLOWED_CHARACTERS.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Custom name may contain only letters, numbers, spaces, hyphens and apostrophes"
            );
        }
        return normalized;
    }

    public static String key(String normalizedValue) {
        return normalizedValue == null ? "" : normalizedValue;
    }
}
