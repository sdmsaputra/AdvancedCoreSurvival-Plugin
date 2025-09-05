package com.minekarta.advancedcoresurvival.core.modules;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the lifecycle of all feature modules.
 * This class is responsible for registering, enabling, and disabling modules based on the plugin configuration.
 */
public class ModuleManager {

    private final AdvancedCoreSurvival plugin;
    private final ConfigManager configManager;
    private final List<Module> modules = new ArrayList<>();
    private final List<Module> enabledModules = new ArrayList<>();

    public ModuleManager(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    /**
     * Checks if a module is currently enabled.
     * @param moduleName The name of the module.
     * @return true if the module is in the enabled list.
     */
    public boolean isModuleEnabled(String moduleName) {
        return enabledModules.stream().anyMatch(module -> module.getName().equalsIgnoreCase(moduleName));
    }

    /**
     * Registers all available modules. This should be called before enabling them.
     * In the future, this is where we will instantiate and add each module implementation.
     * Example: modules.add(new EconomyModule());
     */
    public void registerModules() {
        // This is where all module classes will be instantiated and added to the list.
        modules.add(new com.minekarta.advancedcoresurvival.modules.rpg.RPGModule());
        modules.add(new com.minekarta.advancedcoresurvival.modules.essentials.EssentialsModule());
        modules.add(new com.minekarta.advancedcoresurvival.modules.economy.EconomyModule());
        modules.add(new com.minekarta.advancedcoresurvival.modules.claims.ClaimsModule());
    }

    /**
     * Iterates through all registered modules, checks if they are enabled in the config,
     * and calls their onEnable() method if they are.
     */
    public void enableModules() {
        plugin.getLogger().info("Checking modules...");
        for (Module module : modules) {
            if (configManager.isModuleEnabled(module.getName())) {
                try {
                    module.onEnable(plugin);
                    enabledModules.add(module);
                    plugin.getLogger().info("- Module '" + module.getName() + "' has been enabled.");
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to enable module '" + module.getName() + "'.");
                    e.printStackTrace();
                }
            } else {
                plugin.getLogger().info("- Module '" + module.getName() + "' is disabled in the config.");
            }
        }
        plugin.getLogger().info(enabledModules.size() + " out of " + modules.size() + " modules were enabled.");
    }

    /**
     * Iterates through all enabled modules and calls their onDisable() method.
     */
    public void disableModules() {
        plugin.getLogger().info("Disabling all enabled modules...");
        for (Module module : enabledModules) {
            try {
                module.onDisable();
                plugin.getLogger().info("- Module '" + module.getName() + "' has been disabled.");
            } catch (Exception e) {
                plugin.getLogger().severe("An error occurred while disabling module '" + module.getName() + "'.");
                e.printStackTrace();
            }
        }
        enabledModules.clear();
        plugin.getLogger().info("Module disabling complete.");
    }
}
