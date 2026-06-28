/**
 * CuteInteractions — made by tyhawkey.
 */
package com.cuteinteractions;

import com.cuteinteractions.audit.AuditService;
import com.cuteinteractions.command.AdminCommand;
import com.cuteinteractions.command.InteractionCommand;
import com.cuteinteractions.config.ConfigService;
import com.cuteinteractions.data.PlayerDataService;
import com.cuteinteractions.interaction.InteractionType;
import com.cuteinteractions.service.CooldownService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CuteInteractionsPlugin extends JavaPlugin {
    public static final String SUPPORTED_API = "26.x+";

    private ConfigService configService;
    private PlayerDataService playerDataService;
    private CooldownService cooldownService;
    private AuditService auditService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.configService = new ConfigService(this);
        this.playerDataService = new PlayerDataService(this);
        this.cooldownService = new CooldownService();
        this.auditService = new AuditService(this, configService);

        registerCommands();
        getLogger().info("CuteInteractions enabled for Paper/Purpur API " + SUPPORTED_API + " on Java 21.");
    }

    @Override
    public void onDisable() {
        if (playerDataService != null) {
            playerDataService.save();
        }
        if (auditService != null) {
            auditService.flush();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        configService.reload();
        auditService.reload();
        playerDataService.reload();
    }

    public ConfigService configService() {
        return configService;
    }

    public PlayerDataService playerDataService() {
        return playerDataService;
    }

    public CooldownService cooldownService() {
        return cooldownService;
    }

    public AuditService auditService() {
        return auditService;
    }

    private void registerCommands() {
        InteractionCommand interactionCommand = new InteractionCommand(this);
        for (InteractionType type : InteractionType.values()) {
            PluginCommand command = getCommand(type.id());
            if (command == null) {
                getLogger().warning("Command missing from plugin.yml: " + type.id());
                continue;
            }
            command.setExecutor(interactionCommand);
            command.setTabCompleter(interactionCommand);
        }

        PluginCommand adminCommand = getCommand("cuteinteractions");
        if (adminCommand == null) {
            getLogger().warning("Command missing from plugin.yml: cuteinteractions");
            return;
        }
        AdminCommand admin = new AdminCommand(this);
        adminCommand.setExecutor(admin);
        adminCommand.setTabCompleter(admin);
    }
}
