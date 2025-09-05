package com.minekarta.advancedcoresurvival.modules.economy;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.Module;
import com.minekarta.advancedcoresurvival.integrations.vault.VaultHook;
import com.minekarta.advancedcoresurvival.modules.economy.commands.BalanceCommand;

public class EconomyModule implements Module {

    private VaultHook vaultHook;

    @Override
    public String getName() {
        return "economy";
    }

    @Override
    public void onEnable(AdvancedCoreSurvival plugin) {
        // Register commands
        plugin.getCommand("balance").setExecutor(new BalanceCommand(plugin));
        // plugin.getCommand("pay").setExecutor(new PayCommand(plugin));
        // plugin.getCommand("baltop").setExecutor(new BaltopCommand(plugin));

        // Register listener for new players to give them a starting balance
        plugin.getServer().getPluginManager().registerEvents(new EconomyListener(plugin), plugin);

        // Hook into Vault if it's present
        if (plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            vaultHook = new VaultHook(plugin);
            vaultHook.hook();
        } else {
            plugin.getLogger().warning("Vault not found. The internal economy will work, but other plugins won't be able to use it.");
        }
    }

    @Override
    public void onDisable() {
        if (vaultHook != null) {
            vaultHook.unhook();
        }
    }
}
