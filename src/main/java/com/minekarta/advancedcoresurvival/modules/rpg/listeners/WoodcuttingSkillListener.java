package com.minekarta.advancedcoresurvival.modules.rpg.listeners;

import com.minekarta.advancedcoresurvival.modules.rpg.leveling.LevelingManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;

public class WoodcuttingSkillListener implements Listener {

    private final LevelingManager levelingManager;
    private final double expPerLog = 6.0;

    public WoodcuttingSkillListener(LevelingManager levelingManager) {
        this.levelingManager = levelingManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        Block block = event.getBlock();

        // Use the LOGS tag to identify all types of logs easily.
        if (Tag.LOGS.isTagged(block.getType())) {
            levelingManager.onExpGain(player, expPerLog);
        }
    }
}
