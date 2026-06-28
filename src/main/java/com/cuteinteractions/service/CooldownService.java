/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions.service;

import com.cuteinteractions.interaction.InteractionType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CooldownService {
    private final Map<UUID, Map<InteractionType, Long>> cooldowns = new HashMap<>();

    public long remainingSeconds(UUID playerId, InteractionType type) {
        Map<InteractionType, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return 0;
        }
        Long expiresAt = playerCooldowns.get(type);
        if (expiresAt == null) {
            return 0;
        }
        long remainingMillis = expiresAt - System.currentTimeMillis();
        return remainingMillis <= 0 ? 0 : (long) Math.ceil(remainingMillis / 1000.0);
    }

    public void set(UUID playerId, InteractionType type, long seconds) {
        cooldowns.computeIfAbsent(playerId, ignored -> new EnumMap<>(InteractionType.class))
                .put(type, System.currentTimeMillis() + (seconds * 1000L));
    }

    public void clear(UUID playerId, InteractionType type) {
        Map<InteractionType, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns != null) {
            playerCooldowns.remove(type);
        }
    }

    public void clearAll(UUID playerId) {
        cooldowns.remove(playerId);
    }

    public String describe(UUID playerId) {
        StringBuilder builder = new StringBuilder();
        for (InteractionType type : InteractionType.values()) {
            long remaining = remainingSeconds(playerId, type);
            if (remaining <= 0) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(type.id()).append("=").append(remaining).append("s");
        }
        return builder.isEmpty() ? "none" : builder.toString();
    }
}
