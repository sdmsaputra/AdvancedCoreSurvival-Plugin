package com.minekarta.advancedcoresurvival.modules.rpg.listeners;

import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles loading and unloading of player stats when they connect or disconnect.
 */
public class PlayerConnectionListener implements Listener {

    private final PlayerStatsManager statsManager;

    public PlayerConnectionListener(PlayerStatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Asynchronously load the player's stats into the cache.
        // The getPlayerStats method in the manager handles the initial loading.
        statsManager.getPlayerStats(event.getPlayer());
        // Update their health based on their stats
        statsManager.applyAllBonuses(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Unload the player's stats from the cache to save memory.
        // The manager will handle saving the data before removing it.
        statsManager.unloadPlayer(event.getPlayer().getUniqueId());
    }
}
