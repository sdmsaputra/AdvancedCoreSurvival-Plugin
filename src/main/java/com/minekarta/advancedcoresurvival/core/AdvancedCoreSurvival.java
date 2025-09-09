package com.minekarta.advancedcoresurvival.core;

import com.minekarta.advancedcoresurvival.core.config.ConfigManager;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.core.modules.ModuleManager;
import com.minekarta.advancedcoresurvival.core.storage.StorageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdvancedCoreSurvival extends JavaPlugin {

    private ConfigManager configManager;
    private StorageManager storageManager;
    private ModuleManager moduleManager;
    private Economy economy = null;

    @Override
    public void onEnable() {
        // Step 1: Initialize and load configuration
        configManager = new ConfigManager(this);
        configManager.loadAndSaveDefaults();
        getLogger().info("Configuration loaded.");

        // Step 1.5: Initialize Locale Manager
        LocaleManager.getInstance().loadMessages(this);
        getLogger().info("Locale messages loaded.");

        // Step 2: Initialize Storage
        storageManager = new StorageManager(this);
        storageManager.initializeStorage();

        // Step 3: Initialize Module Manager and modules
        moduleManager = new ModuleManager(this);
        moduleManager.registerModules(); // Finds all available modules
        moduleManager.enableModules();   // Enables modules based on config

        // Step 4: Register Integrations
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.minekarta.advancedcoresurvival.integrations.placeholderapi.ACSExpansion(this).register();
            getLogger().info("Successfully registered PlaceholderAPI expansion.");
        }

        // Register core commands
        getCommand("advancedcoresurvival").setExecutor(new com.minekarta.advancedcoresurvival.core.commands.AdminCommand(this));

        // Step 5: Setup Economy from Vault
        if (!setupEconomy()) {
            getLogger().severe("Vault not found or no economy provider! Economy features will be limited.");
        } else {
            getLogger().info("Successfully hooked into Vault economy.");
        }

        getLogger().info("AdvancedCoreSurvival has been enabled successfully!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    @Override
    public void onDisable() {
        // Disable all modules
        if (moduleManager != null) {
            moduleManager.disableModules();
        }
        // Shut down storage connection
        if (storageManager != null) {
            storageManager.shutdownStorage();
        }
        getLogger().info("AdvancedCoreSurvival has been disabled.");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}
