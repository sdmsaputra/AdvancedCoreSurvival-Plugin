package com.minekarta.advancedcoresurvival.modules.rpg.listeners;

import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for mining-related actions to grant EXP.
 */
public class MiningSkillListener implements Listener {

    private final PlayerStatsManager statsManager;
    private final Map<Material, Double> expValues = new HashMap<>();

    public MiningSkillListener(PlayerStatsManager statsManager) {
        this.statsManager = statsManager;
        // Define EXP values for different blocks
        expValues.put(Material.COAL_ORE, 5.0);
        expValues.put(Material.IRON_ORE, 10.0);
        expValues.put(Material.GOLD_ORE, 15.0);
        expValues.put(Material.DIAMOND_ORE, 25.0);
        expValues.put(Material.EMERALD_ORE, 30.0);
        expValues.put(Material.LAPIS_ORE, 8.0);
        expValues.put(Material.REDSTONE_ORE, 8.0);
        expValues.put(Material.NETHER_QUARTZ_ORE, 7.0);
        expValues.put(Material.STONE, 0.5);
        expValues.put(Material.ANDESITE, 0.5);
        expValues.put(Material.DIORITE, 0.5);
        expValues.put(Material.GRANITE, 0.5);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Only grant EXP if the player is in survival mode
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        Block block = event.getBlock();
        double expToGrant = expValues.getOrDefault(block.getType(), 0.0);

        if (expToGrant > 0) {
            PlayerStats stats = statsManager.getPlayerStats(player);
            if (stats != null) {
                stats.addExp(expToGrant);
                // Optional: Send a message to the player
                // player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("+" + expToGrant + " Mining EXP"));

                // TODO: Add level up logic
            }
        }
    }
}
