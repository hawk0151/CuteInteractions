# Changelog

All notable changes to CuteInteractions are documented here.

## 2.1.0

First public release.

### Features

- Social interaction commands: `/hug`, `/kiss`, `/slap`, `/pat`, `/boop`
- Player-name tab completion on interaction commands
- Per-command cooldowns with bypass permission
- Permissions for every command and admin action
- No self-targeting
- Sounds and particles per interaction
- Coloured broadcast messages via Adventure components
- Configurable messages, cooldowns, sounds, and particles in `config.yml`
- Global incoming interaction toggle with `/ci toggle`
- Admin tools: reload, inspect, cooldown clear, mute, audit, version
- Persistent player settings in `player-data.yml`
- Daily audit files in `audit/YYYY-MM-DD.log`

### Requirements

- Paper or Purpur **26.x+**
- Java **21**

### Notes

- The default `messages.version` string in `config.yml` now uses `%api%` instead of a hard-coded API label. Delete your existing config or update the line manually if upgrading from a pre-release copy.
