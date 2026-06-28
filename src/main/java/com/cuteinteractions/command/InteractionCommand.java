/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions.command;

import com.cuteinteractions.CuteInteractionsPlugin;
import com.cuteinteractions.audit.AuditEntry;
import com.cuteinteractions.config.InteractionSettings;
import com.cuteinteractions.interaction.InteractionType;
import com.cuteinteractions.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class InteractionCommand implements CommandExecutor, TabCompleter {
    private final CuteInteractionsPlugin plugin;

    public InteractionCommand(CuteInteractionsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        InteractionType type = InteractionType.from(command.getName()).orElse(null);
        if (type == null) {
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.configService().message("player-only"));
            return true;
        }

        String permission = "cuteinteractions.command." + type.id();
        if (!player.hasPermission(permission)) {
            player.sendMessage(plugin.configService().message("no-permission", "%permission%", permission));
            audit(player, null, type, "blocked", "no-permission");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(plugin.configService().message("interaction-usage", "%command%", label));
            audit(player, null, type, "blocked", "invalid-usage");
            return true;
        }

        Player target = findOnlinePlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.configService().message("player-not-found"));
            audit(player, null, type, "blocked", "target-offline");
            return true;
        }

        plugin.playerDataService().rememberName(player);
        plugin.playerDataService().rememberName(target);

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(plugin.configService().message("no-self-target"));
            audit(player, target, type, "blocked", "self-target");
            return true;
        }

        if (plugin.playerDataService().isMuted(player.getUniqueId())) {
            String time = TimeUtil.describeSeconds(plugin.playerDataService().mutedRemainingSeconds(player.getUniqueId()));
            player.sendMessage(plugin.configService().message("sender-muted", "%time%", time));
            audit(player, target, type, "blocked", "sender-muted");
            return true;
        }

        if (!plugin.configService().mutedPlayersCanReceive() && plugin.playerDataService().isMuted(target.getUniqueId())) {
            player.sendMessage(plugin.configService().message("target-muted", "%target%", target.getName()));
            audit(player, target, type, "blocked", "target-muted");
            return true;
        }

        if (!plugin.playerDataService().incomingEnabled(target.getUniqueId())) {
            player.sendMessage(plugin.configService().message("target-disabled", "%target%", target.getName()));
            audit(player, target, type, "blocked", "target-disabled");
            return true;
        }

        long remainingSeconds = plugin.cooldownService().remainingSeconds(player.getUniqueId(), type);
        if (remainingSeconds > 0 && !player.hasPermission("cuteinteractions.cooldown.bypass")) {
            player.sendMessage(plugin.configService().message("cooldown",
                    "%seconds%", Long.toString(remainingSeconds),
                    "%action%", type.id()));
            audit(player, target, type, "blocked", "cooldown");
            return true;
        }

        InteractionSettings settings = plugin.configService().interaction(type);
        if (settings == null) {
            player.sendMessage(plugin.configService().message("internal-error"));
            plugin.getLogger().severe("No interaction settings loaded for " + type.id());
            audit(player, target, type, "blocked", "missing-settings");
            return true;
        }
        plugin.cooldownService().set(player.getUniqueId(), type, settings.cooldownSeconds());
        playEffects(target, settings);
        String broadcast = settings.broadcast()
                .replace("%sender%", player.getName())
                .replace("%target%", target.getName());
        Bukkit.broadcast(plugin.configService().color(broadcast));
        audit(player, target, type, "success", "ok");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return List.of();
        }

        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> names = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (sender instanceof Player player && player.getUniqueId().equals(onlinePlayer.getUniqueId())) {
                continue;
            }
            if (onlinePlayer.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                names.add(onlinePlayer.getName());
            }
        }
        return names;
    }

    private void playEffects(Player target, InteractionSettings settings) {
        Location location = target.getLocation().add(0, 1.4, 0);
        try {
            if (settings.particleCount() > 0) {
                target.getWorld().spawnParticle(settings.particle(), location, settings.particleCount(), 0.4, 0.4, 0.4, 0.03);
            }
            target.getWorld().playSound(target.getLocation(), settings.sound(), settings.soundVolume(), settings.soundPitch());
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("Could not play configured CuteInteractions effects: " + ex.getMessage());
        }
    }

    private Player findOnlinePlayer(String name) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(name)) {
                return onlinePlayer;
            }
        }
        return null;
    }

    private void audit(Player sender, Player target, InteractionType type, String result, String reason) {
        plugin.auditService().log(new AuditEntry(
                Instant.now(),
                sender.getUniqueId(),
                sender.getName(),
                target == null ? new java.util.UUID(0L, 0L) : target.getUniqueId(),
                target == null ? "unknown" : target.getName(),
                type,
                result,
                reason
        ));
    }
}
