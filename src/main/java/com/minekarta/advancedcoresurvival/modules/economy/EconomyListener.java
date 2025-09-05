package com.minekarta.advancedcoresurvival.modules.economy;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EconomyListener implements Listener {

    private final AdvancedCoreSurvival plugin;

    public EconomyListener(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore()) {
            // This is the player's first time on the server
            double startingBalance = plugin.getConfigManager().getStartingBalance();
            plugin.getStorageManager().getStorage().setPlayerBalance(player.getUniqueId(), startingBalance);
            plugin.getLogger().info("Gave starting balance of " + startingBalance + " to new player " + player.getName());
        }
    }
}
