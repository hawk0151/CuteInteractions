/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions.command;

import com.cuteinteractions.CuteInteractionsPlugin;
import com.cuteinteractions.audit.AuditEntry;
import com.cuteinteractions.interaction.InteractionType;
import com.cuteinteractions.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class AdminCommand implements CommandExecutor, TabCompleter {
    private static final int AUDIT_PAGE_SIZE = 8;

    private final CuteInteractionsPlugin plugin;

    public AdminCommand(CuteInteractionsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "help" -> sendHelp(sender);
            case "toggle" -> handleToggle(sender, args);
            case "reload" -> handleReload(sender);
            case "inspect" -> handleInspect(sender, args);
            case "cooldown" -> handleCooldown(sender, args);
            case "mute" -> handleMute(sender, args);
            case "audit" -> handleAudit(sender, args);
            case "version" -> handleVersion(sender);
            default -> sender.sendMessage(plugin.configService().message("unknown-command"));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("help", "toggle", "reload", "inspect", "cooldown", "mute", "audit", "version"), args[0]);
        }
        if (args.length == 2 && List.of("toggle", "inspect", "mute", "audit").contains(args[0].toLowerCase(Locale.ROOT))) {
            return onlineNames(args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("cooldown")) {
            return filter(List.of("clear"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("toggle")) {
            return filter(List.of("on", "off"), args[2]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("cooldown")) {
            return onlineNames(args[2]);
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("cooldown")) {
            return filter(List.of("all", "hug", "kiss", "slap", "pat", "boop"), args[3]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("mute")) {
            return filter(List.of("10m", "1h", "1d", "off", "perm"), args[2]);
        }
        return List.of();
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.configService().message("player-only"));
                return;
            }
            if (!require(sender, "cuteinteractions.toggle")) {
                return;
            }
            boolean enabled = !plugin.playerDataService().incomingEnabled(player.getUniqueId());
            plugin.playerDataService().setIncomingEnabled(player, enabled);
            player.sendMessage(plugin.configService().message(enabled ? "toggle-enabled" : "toggle-disabled"));
            return;
        }

        if (!require(sender, "cuteinteractions.admin.toggle")) {
            return;
        }
        if (args.length != 3 || (!args[2].equalsIgnoreCase("on") && !args[2].equalsIgnoreCase("off"))) {
            sender.sendMessage(plugin.configService().message("usage-toggle-admin"));
            return;
        }
        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.configService().message("unknown-player"));
            return;
        }
        boolean enabled = args[2].equalsIgnoreCase("on");
        plugin.playerDataService().setIncomingEnabled(target, enabled);
        sender.sendMessage(plugin.configService().message(
                enabled ? "admin-toggle-enabled" : "admin-toggle-disabled",
                "%player%", displayName(target)));
    }

    private void handleReload(CommandSender sender) {
        if (!require(sender, "cuteinteractions.admin.reload")) {
            return;
        }
        plugin.reloadPlugin();
        sender.sendMessage(plugin.configService().message("reload"));
    }

    private void handleVersion(CommandSender sender) {
        sender.sendMessage(plugin.configService().message("version",
                "%version%", plugin.getPluginMeta().getVersion(),
                "%api%", CuteInteractionsPlugin.SUPPORTED_API));
    }

    private void handleInspect(CommandSender sender, String[] args) {
        if (!require(sender, "cuteinteractions.admin.inspect")) {
            return;
        }
        if (args.length != 2) {
            sender.sendMessage(plugin.configService().message("usage-inspect"));
            return;
        }
        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.configService().message("unknown-player"));
            return;
        }
        String muted = plugin.playerDataService().isMuted(target.getUniqueId())
                ? TimeUtil.describeSeconds(plugin.playerDataService().mutedRemainingSeconds(target.getUniqueId()))
                : "no";
        sender.sendMessage(plugin.configService().message("inspect",
                "%player%", displayName(target),
                "%incoming%", plugin.playerDataService().incomingEnabled(target.getUniqueId()) ? "enabled" : "disabled",
                "%muted%", muted,
                "%cooldowns%", plugin.cooldownService().describe(target.getUniqueId())));
    }

    private void handleCooldown(CommandSender sender, String[] args) {
        if (!require(sender, "cuteinteractions.admin.cooldown")) {
            return;
        }
        if (args.length < 3 || !args[1].equalsIgnoreCase("clear")) {
            sender.sendMessage(plugin.configService().message("usage-cooldown"));
            return;
        }
        OfflinePlayer target = resolvePlayer(args[2]);
        if (target == null) {
            sender.sendMessage(plugin.configService().message("unknown-player"));
            return;
        }
        String action = args.length >= 4 ? args[3].toLowerCase(Locale.ROOT) : "all";
        if (action.equals("all")) {
            plugin.cooldownService().clearAll(target.getUniqueId());
        } else {
            Optional<InteractionType> type = InteractionType.from(action);
            if (type.isEmpty()) {
                sender.sendMessage(plugin.configService().message("invalid-action"));
                return;
            }
            plugin.cooldownService().clear(target.getUniqueId(), type.get());
        }
        sender.sendMessage(plugin.configService().message("cooldown-cleared", "%player%", displayName(target)));
    }

    private void handleMute(CommandSender sender, String[] args) {
        if (!require(sender, "cuteinteractions.admin.mute")) {
            return;
        }
        if (args.length != 3) {
            sender.sendMessage(plugin.configService().message("usage-mute"));
            return;
        }
        OfflinePlayer target = resolvePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.configService().message("unknown-player"));
            return;
        }
        if (args[2].equalsIgnoreCase("off")) {
            plugin.playerDataService().unmute(target);
            sender.sendMessage(plugin.configService().message("unmuted", "%player%", displayName(target)));
            return;
        }
        long durationMillis = TimeUtil.parseDurationMillis(args[2]);
        if (durationMillis < 0) {
            sender.sendMessage(plugin.configService().message("invalid-duration"));
            return;
        }
        plugin.playerDataService().mute(target, durationMillis);
        String time = durationMillis == Long.MAX_VALUE ? "permanent" : TimeUtil.describeSeconds(durationMillis / 1000L);
        sender.sendMessage(plugin.configService().message("muted",
                "%player%", displayName(target),
                "%time%", time));
    }

    private void handleAudit(CommandSender sender, String[] args) {
        if (!require(sender, "cuteinteractions.admin.audit")) {
            return;
        }
        if (args.length > 3) {
            sender.sendMessage(plugin.configService().message("usage-audit"));
            return;
        }
        String filter = null;
        int page = 1;
        if (args.length >= 2) {
            if (isInteger(args[1])) {
                page = Math.max(1, Integer.parseInt(args[1]));
            } else {
                filter = args[1];
            }
        }
        if (args.length >= 3) {
            if (!isInteger(args[2])) {
                sender.sendMessage(plugin.configService().message("usage-audit"));
                return;
            }
            page = Math.max(1, Integer.parseInt(args[2]));
        }

        List<AuditEntry> entries = plugin.auditService().recent(filter);
        if (entries.isEmpty()) {
            sender.sendMessage(plugin.configService().message("audit-empty"));
            return;
        }
        int pages = Math.max(1, (int) Math.ceil(entries.size() / (double) AUDIT_PAGE_SIZE));
        page = Math.min(page, pages);
        sender.sendMessage(plugin.configService().message("audit-header",
                "%page%", Integer.toString(page),
                "%pages%", Integer.toString(pages)));
        int start = (page - 1) * AUDIT_PAGE_SIZE;
        int end = Math.min(entries.size(), start + AUDIT_PAGE_SIZE);
        for (AuditEntry entry : entries.subList(start, end)) {
            sender.sendMessage(plugin.configService().message("audit-line",
                    "%time%", plugin.auditService().displayTime(entry),
                    "%sender%", entry.senderName(),
                    "%target%", entry.targetName(),
                    "%action%", entry.action().id(),
                    "%result%", entry.result(),
                    "%reason%", entry.reason()));
        }
    }

    private boolean require(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(plugin.configService().message("no-permission", "%permission%", permission));
        return false;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.configService().message("help-header"));
        sender.sendMessage(plugin.configService().message("help-line-toggle"));
        sender.sendMessage(plugin.configService().message("help-line-interactions"));
        if (sender.hasPermission("cuteinteractions.admin.*")
                || sender.hasPermission("cuteinteractions.admin.reload")
                || sender.hasPermission("cuteinteractions.admin.inspect")
                || sender.hasPermission("cuteinteractions.admin.toggle")
                || sender.hasPermission("cuteinteractions.admin.cooldown")
                || sender.hasPermission("cuteinteractions.admin.mute")
                || sender.hasPermission("cuteinteractions.admin.audit")) {
            sender.sendMessage(plugin.configService().message("help-line-admin"));
        }
    }

    private OfflinePlayer resolvePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
            if (offline.getName() != null && offline.getName().equalsIgnoreCase(name)) {
                return offline;
            }
        }
        return null;
    }

    private String displayName(OfflinePlayer player) {
        return player.getName() == null ? player.getUniqueId().toString() : player.getName();
    }

    private List<String> onlineNames(String prefix) {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return filter(names, prefix);
    }

    private List<String> filter(List<String> values, String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(normalized))
                .toList();
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
