package com.minekarta.advancedcoresurvival.modules.rpg.listeners;

import com.minekarta.advancedcoresurvival.modules.rpg.leveling.LevelingManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;

public class FarmingSkillListener implements Listener {

    private final LevelingManager levelingManager;
    private final Map<Material, Double> expValues = new HashMap<>();

    public FarmingSkillListener(LevelingManager levelingManager) {
        this.levelingManager = levelingManager;
        // Define EXP values for different crops
        expValues.put(Material.WHEAT, 4.0);
        expValues.put(Material.CARROTS, 4.0);
        expValues.put(Material.POTATOES, 4.0);
        expValues.put(Material.BEETROOTS, 4.0);
        expValues.put(Material.NETHER_WART, 5.0);
        expValues.put(Material.PUMPKIN, 8.0);
        expValues.put(Material.MELON, 8.0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        Block block = event.getBlock();
        double expToGrant = expValues.getOrDefault(block.getType(), 0.0);

        if (expToGrant > 0) {
            // For ageable crops, only grant EXP if they are fully grown
            if (block.getBlockData() instanceof Ageable) {
                Ageable ageable = (Ageable) block.getBlockData();
                if (ageable.getAge() != ageable.getMaximumAge()) {
                    return; // Not fully grown, no EXP
                }
            }

            // For pumpkins and melons, they are not ageable, so they grant EXP directly.

            levelingManager.onExpGain(player, expToGrant);
        }
    }
}
