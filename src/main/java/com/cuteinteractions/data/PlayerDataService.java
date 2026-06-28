/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions.data;

import com.cuteinteractions.CuteInteractionsPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class PlayerDataService {
    private final CuteInteractionsPlugin plugin;
    private final File file;
    private YamlConfiguration data;

    public PlayerDataService(CuteInteractionsPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player-data.yml");
        reload();
    }

    public void reload() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder.");
        }
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public boolean incomingEnabled(UUID playerId) {
        return data.getBoolean(path(playerId, "incoming-enabled"), true);
    }

    public void setIncomingEnabled(OfflinePlayer player, boolean enabled) {
        rememberName(player);
        data.set(path(player.getUniqueId(), "incoming-enabled"), enabled);
        save();
    }

    public long mutedUntil(UUID playerId) {
        return data.getLong(path(playerId, "muted-until"), 0L);
    }

    public boolean isMuted(UUID playerId) {
        long until = mutedUntil(playerId);
        return until == Long.MAX_VALUE || until > System.currentTimeMillis();
    }

    public long mutedRemainingSeconds(UUID playerId) {
        long until = mutedUntil(playerId);
        if (until == Long.MAX_VALUE) {
            return Long.MAX_VALUE;
        }
        long remaining = until - System.currentTimeMillis();
        return remaining <= 0 ? 0 : (long) Math.ceil(remaining / 1000.0);
    }

    public void mute(OfflinePlayer player, long durationMillis) {
        rememberName(player);
        long mutedUntil = durationMillis == Long.MAX_VALUE ? Long.MAX_VALUE : System.currentTimeMillis() + durationMillis;
        data.set(path(player.getUniqueId(), "muted-until"), mutedUntil);
        save();
    }

    public void unmute(OfflinePlayer player) {
        rememberName(player);
        data.set(path(player.getUniqueId(), "muted-until"), null);
        save();
    }

    public void rememberName(OfflinePlayer player) {
        if (player.getName() != null) {
            data.set(path(player.getUniqueId(), "name"), player.getName());
        }
    }

    public void save() {
        try {
            data.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save player-data.yml: " + ex.getMessage());
        }
    }

    private String path(UUID playerId, String key) {
        return "players." + playerId + "." + key;
    }
}
