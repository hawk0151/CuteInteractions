/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions.audit;

import com.cuteinteractions.CuteInteractionsPlugin;
import com.cuteinteractions.config.ConfigService;
import org.bukkit.Bukkit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

public final class AuditService {
    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final CuteInteractionsPlugin plugin;
    private final ConfigService config;
    private final Deque<AuditEntry> recent = new ArrayDeque<>();
    private final Deque<String> pendingLines = new ArrayDeque<>();
    private boolean writeScheduled;
    private int cacheSize;

    public AuditService(CuteInteractionsPlugin plugin, ConfigService config) {
        this.plugin = plugin;
        this.config = config;
        reload();
    }

    public synchronized void reload() {
        cacheSize = config.auditCacheSize();
        trimRecent();
    }

    public void log(AuditEntry entry) {
        synchronized (this) {
            recent.addFirst(entry);
            trimRecent();
            pendingLines.addLast(formatFileLine(entry));
            scheduleWrite();
        }
    }

    public synchronized List<AuditEntry> recent(String playerFilter) {
        String normalized = playerFilter == null ? null : playerFilter.toLowerCase(Locale.ROOT);
        List<AuditEntry> entries = new ArrayList<>();
        for (AuditEntry entry : recent) {
            if (normalized == null
                    || entry.senderName().toLowerCase(Locale.ROOT).contains(normalized)
                    || entry.targetName().toLowerCase(Locale.ROOT).contains(normalized)) {
                entries.add(entry);
            }
        }
        return entries;
    }

    public String displayTime(AuditEntry entry) {
        return DISPLAY_TIME.format(entry.timestamp());
    }

    public void flush() {
        List<String> lines = drainPending();
        if (!lines.isEmpty()) {
            writeLines(lines);
        }
    }

    private void scheduleWrite() {
        if (writeScheduled) {
            return;
        }
        writeScheduled = true;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> lines = drainPending();
            writeLines(lines);
        });
    }

    private synchronized List<String> drainPending() {
        List<String> lines = new ArrayList<>(pendingLines);
        pendingLines.clear();
        writeScheduled = false;
        return lines;
    }

    private void writeLines(List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        File folder = new File(plugin.getDataFolder(), "audit");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create audit folder.");
            return;
        }
        File file = new File(folder, FILE_DATE.format(java.time.Instant.now()) + ".log");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not write audit log: " + ex.getMessage());
        }
    }

    private String formatFileLine(AuditEntry entry) {
        return entry.timestamp()
                + " sender=" + entry.senderName() + "(" + entry.senderId() + ")"
                + " target=" + entry.targetName() + "(" + entry.targetId() + ")"
                + " action=" + entry.action().id()
                + " result=" + entry.result()
                + " reason=\"" + entry.reason() + "\"";
    }

    private void trimRecent() {
        while (recent.size() > cacheSize) {
            recent.removeLast();
        }
    }
}
