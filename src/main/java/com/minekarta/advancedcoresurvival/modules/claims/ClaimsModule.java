package com.minekarta.advancedcoresurvival.modules.claims;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.Module;
import com.minekarta.advancedcoresurvival.modules.claims.commands.ClaimCommand;
import com.minekarta.advancedcoresurvival.modules.claims.tax.ClaimTaxManager;

public class ClaimsModule implements Module {

    private ClaimTaxManager taxManager;

    @Override
    public String getName() {
        return "claims";
    }

    @Override
    public void onEnable(AdvancedCoreSurvival plugin) {
        plugin.getLogger().info("Claims module enabled. Registering commands and listeners...");

        // Initialize and start the tax manager
        taxManager = new ClaimTaxManager(plugin);
        taxManager.start();

        // Register the main /claim command handler
        plugin.getCommand("claim").setExecutor(new ClaimCommand(plugin));

        // Register listeners for claim protection
        plugin.getServer().getPluginManager().registerEvents(new ClaimProtectionListener(plugin), plugin);
    }

    public ClaimTaxManager getTaxManager() {
        return taxManager;
    }

    @Override
    public void onDisable() {
        if (taxManager != null) {
            taxManager.stop();
        }
    }
}
