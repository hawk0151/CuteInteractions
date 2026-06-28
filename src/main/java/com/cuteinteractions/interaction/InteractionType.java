/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions.interaction;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum InteractionType {
    HUG("hug"),
    KISS("kiss"),
    SLAP("slap"),
    PAT("pat"),
    BOOP("boop");

    private final String id;

    InteractionType(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<InteractionType> from(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(type -> type.id.equals(normalized))
                .findFirst();
    }
}
