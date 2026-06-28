/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions.config;

import org.bukkit.Particle;
import org.bukkit.Sound;

public record InteractionSettings(
        long cooldownSeconds,
        String broadcast,
        Sound sound,
        float soundVolume,
        float soundPitch,
        Particle particle,
        int particleCount
) {
}
