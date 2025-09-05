package com.minekarta.advancedcoresurvival.core.config;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final AdvancedCoreSurvival plugin;
    private FileConfiguration config;

    public ConfigManager(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads the plugin's configuration from config.yml.
     * If the file doesn't exist, it's created from the defaults in the JAR.
     */
    public void loadAndSaveDefaults() {
        plugin.saveDefaultConfig();
        // We need to reload the config after saving the default to ensure we have the latest values
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    /**
     * Checks if a specific module is enabled in the configuration.
     * @param moduleName The name of the module (e.g., "claims", "economy").
     * @return true if the module is enabled, false otherwise.
     */
    public boolean isModuleEnabled(String moduleName) {
        return config.getBoolean("modules." + moduleName, false);
    }

    /**
     * Gets the configured storage type.
     * @return The storage type string (e.g., "sqlite").
     */
    public String getStorageType() {
        return config.getString("storage.type", "sqlite");
    }

    /**
     * Gets the configured locale/language.
     * @return The locale string (e.g., "en_US").
     */
    public String getLocale() {
        return config.getString("locale", "en_US");
    }

    /**
     * Checks if debug mode is enabled.
     * @return true if debug mode is on, false otherwise.
     */
    public boolean isDebugMode() {
        return config.getBoolean("debug", false);
    }

    // We can add more specific getters for other configuration sections as needed.
    // For example, for nested mysql settings:
    public String getMySqlHost() {
        return config.getString("storage.mysql.host");
    }

    // --- Essentials ---
    public int getMaxHomes() {
        return config.getInt("essentials.max-homes", 3);
    }

    // --- Economy ---
    public String getCurrencySymbol() {
        return config.getString("economy.currency-symbol", "$");
    }

    public double getStartingBalance() {
        return config.getDouble("economy.starting-balance", 1000.0);
    }

    // --- Claims ---
    public int getMaxClaimsPerPlayer() {
        return config.getInt("claims.internal.max-claims-per-player", 3);
    }
}
