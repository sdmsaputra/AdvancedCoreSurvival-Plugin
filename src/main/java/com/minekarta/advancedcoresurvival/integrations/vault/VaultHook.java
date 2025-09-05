package com.minekarta.advancedcoresurvival.integrations.vault;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;

public class VaultHook {

    private final AdvancedCoreSurvival plugin;
    private ACSEconomy economyProvider;

    public VaultHook(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempts to hook into Vault and register the economy provider.
     */
    public void hook() {
        try {
            economyProvider = new ACSEconomy(plugin);
            ServicesManager sm = plugin.getServer().getServicesManager();
            sm.register(Economy.class, economyProvider, plugin, ServicePriority.Normal);
            plugin.getLogger().info("Successfully hooked into Vault and registered the ACS economy provider.");
        } catch (Exception e) {
            plugin.getLogger().severe("Could not hook into Vault. Economy features will not be available to other plugins.");
            e.printStackTrace();
        }
    }

    /**
     * Unhooks from Vault, unregistering the economy provider.
     */
    public void unhook() {
        if (economyProvider != null) {
            try {
                plugin.getServer().getServicesManager().unregister(Economy.class, economyProvider);
                plugin.getLogger().info("Successfully unhooked from Vault.");
            } catch (Exception e) {
                plugin.getLogger().severe("Error while unhooking from Vault.");
                e.printStackTrace();
            }
        }
    }
}
