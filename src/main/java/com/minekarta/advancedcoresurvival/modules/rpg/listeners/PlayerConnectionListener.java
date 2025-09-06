package com.minekarta.advancedcoresurvival.modules.rpg.listeners;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import com.minekarta.advancedcoresurvival.modules.rpg.skills.SkillManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles loading/unloading of player stats and applying bonuses on connect.
 */
public class PlayerConnectionListener implements Listener {

    private final PlayerStatsManager statsManager;
    private final SkillManager skillManager;
    private final AdvancedCoreSurvival plugin;

    public PlayerConnectionListener(PlayerStatsManager statsManager, SkillManager skillManager, AdvancedCoreSurvival plugin) {
        this.statsManager = statsManager;
        this.skillManager = skillManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Asynchronously load stats, then apply bonuses on the main thread.
        statsManager.getPlayerStatsAsync(player.getUniqueId()).thenAccept(stats -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                applyAllBonuses(player, stats);
            });
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Unload the player's stats from the cache, which also triggers a save.
        statsManager.unloadPlayer(event.getPlayer().getUniqueId());
    }

    /**
     * Applies all permanent stat bonuses (health, armor, etc.) to a player.
     */
    private void applyAllBonuses(Player player, PlayerStats stats) {
        if (stats == null) return;

        // Recalculate bonuses based on loaded skill levels
        stats.recalculateStatBonuses(skillManager);

        // Apply health bonus from Endurance
        double healthPerEndurance = plugin.getConfig().getDouble("rpg.stats.health-per-endurance", 1.0);
        double bonusHealth = stats.getEndurance() * healthPerEndurance;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0 + bonusHealth);

        // Apply armor bonus from skills
        double armorBonus = stats.getStatBonus("ARMOR_BONUS");
        player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armorBonus);
    }
}
