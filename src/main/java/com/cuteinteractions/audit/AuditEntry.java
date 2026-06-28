/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions.audit;

import com.cuteinteractions.interaction.InteractionType;

import java.time.Instant;
import java.util.UUID;

public record AuditEntry(
        Instant timestamp,
        UUID senderId,
        String senderName,
        UUID targetId,
        String targetName,
        InteractionType action,
        String result,
        String reason
) {
}
