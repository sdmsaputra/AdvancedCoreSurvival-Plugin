# AdvancedCoreSurvival

![Java CI with Maven](https://github.com/Minekarta-Studio/AdvancedCoreSurvival/actions/workflows/maven.yml/badge.svg)

**AdvancedCoreSurvival** is a comprehensive, modular survival core plugin for PaperMC servers, designed to provide a premium, all-in-one experience. Developed by **Minekarta Studio**, it combines essentials, economy, land claiming, and RPG features into a single, highly configurable package.

This plugin was proudly developed with the assistance of an advanced AI software engineer.

---

## ✨ Features

AdvancedCoreSurvival is built on a powerful modular system. You can enable or disable features in the `config.yml` to tailor the plugin to your server's needs.

### Currently Implemented Core Features:

*   **🔌 Modular System**: Enable and disable modules on the fly.
*   **💾 Flexible Data Storage**: Supports SQLite out-of-the-box with an extensible design for MySQL. All database operations are performed **asynchronously** to prevent server lag.
*   **🏠 Survival Essentials**:
    *   `/setspawn` & `/spawn`: Set and teleport to the server's global spawn point.
    *   `/sethome`, `/home`, `/delhome`: Allow players to set, manage, and teleport to their personal homes. Home count is configurable.
*   **💰 Economy**:
    *   `/balance`: Check a player's balance.
    *   **Starting Balance**: Automatically give new players a configurable starting balance.
    *   **Vault Integration**: Hooks directly into Vault, providing a robust economy provider for other plugins to use.
*   **🔒 Land Claiming**:
    *   `/claim create`: Players can claim the chunk they are standing in.
    *   **Claim Protection**: Automatically prevents non-members from breaking or placing blocks in claimed chunks.
    *   **Configurable Limits**: Set a maximum number of claims per player.
*   **🌐 Integrations**:
    *   **Vault**: Full support for the Vault economy API.
    *   **PlaceholderAPI**: Provides a wide range of placeholders for use in other plugins.

### Planned Features (from `documentation.md`):

*   Full implementation of all commands for Essentials, Economy, and Claims.
*   Advanced RPG and MMORPG modules with skills, classes, and abilities.
*   Party and Team systems for player grouping.
*   In-game shops, auction houses, and more.

---

## 🚀 Installation

1.  Download the latest release of `AdvancedCoreSurvival.jar` from the [releases page](https://github.com/Minekarta-Studio/AdvancedCoreSurvival/releases).
2.  Place the `.jar` file into your server's `plugins/` folder.
3.  (Required) Install [Vault](https://www.spigotmc.org/resources/vault.34315/) and [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).
4.  Start your server once to generate the default configuration files.
5.  Edit `/plugins/AdvancedCoreSurvival/config.yml` to enable the modules and features you want.
6.  Restart the server or use `/acs reload` (when implemented).

---

## 📖 Documentation

For a complete guide to all features, commands, permissions, and configuration options, please see the **[Full Technical Documentation](documentation.md)**.

This includes:
- Detailed configuration for every module.
- A complete list of all commands and permissions.
- Information for developers on how to use the API.

---

## 💬 Support

If you encounter any bugs or have a feature request, please [open an issue](https://github.com/Minekarta-Studio/AdvancedCoreSurvival/issues) on our GitHub repository. Please include your server version, plugin version, and any relevant logs or configuration files.
