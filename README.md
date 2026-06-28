# CuteInteractions

CuteInteractions is a Paper/Purpur **26.x+** plugin that adds moderated cute player interaction commands for multiplayer servers.

Made by **tyhawkey**.

## Requirements

- [Paper](https://papermc.io/downloads/paper) or [Purpur](https://purpurmc.org/downloads) **26.x+**
- Java **21**

## Installation

1. Download `CuteInteractions-2.1.0.jar` from the [releases page](https://github.com/tyhawkey/CuteInteractions/releases).
2. Place the JAR in your server's `plugins/` folder.
3. Restart the server (or use a plugin load command if your setup supports it).
4. Edit `plugins/CuteInteractions/config.yml` to customise messages, cooldowns, sounds, and particles.

On first run the plugin creates:

- `plugins/CuteInteractions/config.yml` — main configuration
- `plugins/CuteInteractions/player-data.yml` — per-player toggle and mute state
- `plugins/CuteInteractions/audit/` — daily audit log files

## Commands

| Command | Description |
|---------|-------------|
| `/hug <player>` | Hug another player |
| `/kiss <player>` | Kiss another player |
| `/slap <player>` | Slap another player |
| `/pat <player>` | Pat another player |
| `/boop <player>` | Boop another player |
| `/ci` or `/cuteinteractions` | Admin and player settings (alias: `/ci`) |

### `/ci` subcommands

| Subcommand | Description |
|------------|-------------|
| `/ci toggle` | Toggle whether others can use interactions on you |
| `/ci toggle <player> <on\|off>` | Admin: change a player's incoming setting |
| `/ci reload` | Reload configuration |
| `/ci inspect <player>` | View player state |
| `/ci cooldown clear <player> [action\|all]` | Clear cooldowns |
| `/ci mute <player> <duration\|off>` | Mute a player from using interactions |
| `/ci audit [player] [page]` | View recent audit entries |
| `/ci version` | Show plugin version |

## Permissions

```text
cuteinteractions.command.hug
cuteinteractions.command.kiss
cuteinteractions.command.slap
cuteinteractions.command.pat
cuteinteractions.command.boop
cuteinteractions.cooldown.bypass
cuteinteractions.toggle
cuteinteractions.admin.reload
cuteinteractions.admin.inspect
cuteinteractions.admin.toggle
cuteinteractions.admin.cooldown
cuteinteractions.admin.mute
cuteinteractions.admin.audit
cuteinteractions.admin.*
cuteinteractions.*
```

Most interaction permissions default to `true` for all players. Admin permissions default to `op`.

## Configuration

`config.yml` is split into four sections:

- **audit** — in-memory cache size for recent audit entries
- **moderation** — whether muted players can still receive interactions
- **messages** — all in-game text, including the plugin prefix
- **interactions** — per-command cooldown, broadcast message, sound, and particle settings

Sound names use Minecraft registry IDs (for example `ENTITY_PLAYER_LEVELUP`). Particle names use Bukkit enum names (for example `HEART`).

## Build from source

```bash
mvn clean package
```

The plugin JAR is written to:

```text
target/CuteInteractions-2.1.0.jar
```

## Author

Made by **tyhawkey**.

## License

MIT — see [LICENSE](LICENSE).
