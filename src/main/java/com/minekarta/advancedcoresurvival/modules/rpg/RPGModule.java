package com.minekarta.advancedcoresurvival.modules.rpg;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.Module;
import com.minekarta.advancedcoresurvival.modules.rpg.commands.StatsCommand;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import com.minekarta.advancedcoresurvival.modules.rpg.listeners.MiningSkillListener;
import com.minekarta.advancedcoresurvival.modules.rpg.listeners.PlayerConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RPGModule implements Module {

    private PlayerStatsManager statsManager;

    @Override
    public String getName() {
        return "rpg";
    }

    @Override
    public void onEnable(AdvancedCoreSurvival plugin) {
        plugin.getLogger().info("Initializing RPG Module...");

        // Initialize managers
        this.statsManager = new PlayerStatsManager();

        // Register Listeners
        plugin.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(statsManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MiningSkillListener(statsManager), plugin);

        // Register Commands
        plugin.getCommand("stats").setExecutor(new StatsCommand(statsManager));

        // Load stats for any players who are already online (e.g., on a /reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            statsManager.loadPlayerStats(player.getUniqueId());
        }
    }

    @Override
    public void onDisable() {
        // Save stats for all online players when the module is disabled
        if (statsManager != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                statsManager.unloadPlayer(player.getUniqueId());
            }
        }
    }
}
