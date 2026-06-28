/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions.config;

import com.cuteinteractions.CuteInteractionsPlugin;
import com.cuteinteractions.interaction.InteractionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class ConfigService {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final CuteInteractionsPlugin plugin;
    private final Map<InteractionType, InteractionSettings> interactions = new EnumMap<>(InteractionType.class);
    private int auditCacheSize;
    private boolean mutedPlayersCanReceive;

    public ConfigService(CuteInteractionsPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        interactions.clear();
        FileConfiguration config = plugin.getConfig();
        auditCacheSize = Math.max(10, config.getInt("audit.recent-cache-size", 200));
        mutedPlayersCanReceive = config.getBoolean("moderation.muted-players-can-receive", true);

        for (InteractionType type : InteractionType.values()) {
            String path = "interactions." + type.id() + ".";
            long cooldownSeconds = Math.max(0, config.getLong(path + "cooldown-seconds", 10));
            String broadcast = config.getString(path + "broadcast", "&d%sender% &fuses /" + type.id() + " on &d%target%&f!");
            Sound sound = soundValue(config.getString(path + "sound"), Sound.UI_BUTTON_CLICK);
            float soundVolume = (float) config.getDouble(path + "sound-volume", 0.8);
            float soundPitch = (float) config.getDouble(path + "sound-pitch", 1.2);
            Particle particle = particleValue(config.getString(path + "particle"), Particle.HEART);
            int particleCount = Math.max(0, config.getInt(path + "particle-count", 8));

            interactions.put(type, new InteractionSettings(
                    cooldownSeconds,
                    broadcast,
                    sound,
                    soundVolume,
                    soundPitch,
                    particle,
                    particleCount
            ));
        }
    }

    public InteractionSettings interaction(InteractionType type) {
        return interactions.get(type);
    }

    public int auditCacheSize() {
        return auditCacheSize;
    }

    public boolean mutedPlayersCanReceive() {
        return mutedPlayersCanReceive;
    }

    public Component message(String key) {
        return color(plugin.getConfig().getString("messages.prefix", "") + plugin.getConfig().getString("messages." + key, ""));
    }

    public Component message(String key, String... replacements) {
        String text = plugin.getConfig().getString("messages.prefix", "") + plugin.getConfig().getString("messages." + key, "");
        for (int index = 0; index + 1 < replacements.length; index += 2) {
            text = text.replace(replacements[index], replacements[index + 1]);
        }
        return color(text);
    }

    public Component color(String text) {
        return LEGACY.deserialize(text == null ? "" : text);
    }

    private Sound soundValue(String value, Sound fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        NamespacedKey key = normalized.contains(":")
                ? NamespacedKey.fromString(normalized)
                : NamespacedKey.minecraft(normalized.replace('_', '.'));
        Sound sound = key == null ? null : Registry.SOUNDS.get(key);
        if (sound == null) {
            plugin.getLogger().warning("Invalid Sound in config: " + value + ". Using fallback sound.");
            return fallback;
        }
        return sound;
    }

    private Particle particleValue(String value, Particle fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Particle.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Invalid Particle in config: " + value + ". Using " + fallback.name());
            return fallback;
        }
    }
}
