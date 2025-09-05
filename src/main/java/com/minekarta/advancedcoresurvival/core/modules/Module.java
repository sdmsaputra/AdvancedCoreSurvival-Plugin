package com.minekarta.advancedcoresurvival.core.modules;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;

/**
 * Represents a feature module within AdvancedCoreSurvival.
 * Each module can be enabled or disabled via the config.
 */
public interface Module {

    /**
     * The name of the module. This should match the key in the `modules` section of config.yml.
     * @return The module's name (e.g., "economy", "claims").
     */
    String getName();

    /**
     * Called when the module is being enabled.
     * This is where listeners and commands should be registered.
     * @param plugin The main plugin instance.
     */
    void onEnable(AdvancedCoreSurvival plugin);

    /**
     * Called when the module is being disabled.
     * This is where any resources should be cleaned up.
     */
    void onDisable();
}
