package com.minekarta.advancedcoresurvival.modules.essentials;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.Module;
import com.minekarta.advancedcoresurvival.modules.essentials.commands.DelHomeCommand;
import com.minekarta.advancedcoresurvival.modules.essentials.commands.HomeCommand;
import com.minekarta.advancedcoresurvival.modules.essentials.commands.SetHomeCommand;
import com.minekarta.advancedcoresurvival.modules.essentials.commands.SetSpawnCommand;
import com.minekarta.advancedcoresurvival.modules.essentials.commands.SpawnCommand;

/**
 * The module for essential survival commands like /spawn, /home, /tpa, etc.
 */
public class EssentialsModule implements Module {

    @Override
    public String getName() {
        // This name MUST match the key in the `modules` section of config.yml
        return "survival-essentials";
    }

    @Override
    public void onEnable(AdvancedCoreSurvival plugin) {
        // Register all essential commands
        plugin.getCommand("setspawn").setExecutor(new SetSpawnCommand(plugin));
        plugin.getCommand("spawn").setExecutor(new SpawnCommand(plugin));
        plugin.getCommand("sethome").setExecutor(new SetHomeCommand(plugin));
        plugin.getCommand("home").setExecutor(new HomeCommand(plugin));
        plugin.getCommand("delhome").setExecutor(new DelHomeCommand(plugin));

        // We would also register tab completers here
        // plugin.getCommand("home").setTabCompleter(new HomeTabCompleter(plugin));
    }

    @Override
    public void onDisable() {
        // In a more complex module, you might want to clear caches or save data here.
        // For essentials, most data is on-demand, so there's little to do on disable.
    }
}
