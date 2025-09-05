package com.minekarta.advancedcoresurvival.modules.claims;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.Module;
import com.minekarta.advancedcoresurvival.modules.claims.commands.ClaimCommand;

public class ClaimsModule implements Module {
    @Override
    public String getName() {
        return "claims";
    }

    @Override
    public void onEnable(AdvancedCoreSurvival plugin) {
        plugin.getLogger().info("Claims module enabled. Registering commands and listeners...");
        // Register the main /claim command handler
        plugin.getCommand("claim").setExecutor(new ClaimCommand(plugin));

        // Register listeners for claim protection
        plugin.getServer().getPluginManager().registerEvents(new ClaimProtectionListener(plugin), plugin);
    }

    @Override
    public void onDisable() {
        // Logic to run when the module is disabled
    }
}
