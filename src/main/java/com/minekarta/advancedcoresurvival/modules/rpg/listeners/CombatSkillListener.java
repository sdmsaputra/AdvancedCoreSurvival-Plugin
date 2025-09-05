package com.minekarta.advancedcoresurvival.modules.rpg.listeners;

import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import com.minekarta.advancedcoresurvival.modules.rpg.leveling.LevelingManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener for combat-related actions to grant EXP and apply stat effects.
 */
public class CombatSkillListener implements Listener {

    private final LevelingManager levelingManager;
    private final PlayerStatsManager statsManager;
    private final Map<EntityType, Double> expValues;
    private final double damagePerStrength;
    private final double dodgeChancePerAgility;

    public CombatSkillListener(LevelingManager levelingManager, PlayerStatsManager statsManager, Map<EntityType, Double> expValues, double damagePerStrength, double dodgeChancePerAgility) {
        this.levelingManager = levelingManager;
        this.statsManager = statsManager;
        this.expValues = expValues;
        this.damagePerStrength = damagePerStrength;
        this.dodgeChancePerAgility = dodgeChancePerAgility;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        // Only grant EXP if the killer is a player
        if (event.getEntity().getKiller() == null) {
            return;
        }

        Player player = event.getEntity().getKiller();
        EntityType entityType = event.getEntityType();

        double expToGrant = expValues.getOrDefault(entityType, 0.0);

        if (expToGrant > 0) {
            levelingManager.onExpGain(player, expToGrant);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // --- Agility (Dodge Chance) ---
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            PlayerStats victimStats = statsManager.getPlayerStats(victim);
            if (victimStats != null) {
                double dodgeChance = victimStats.getAgility() * dodgeChancePerAgility;
                if (ThreadLocalRandom.current().nextDouble() < dodgeChance) {
                    event.setCancelled(true);
                    // TODO: Add a "dodge" message or sound effect
                    return; // Stop processing damage if dodged
                }
            }
        }

        // --- Strength (Damage Bonus) ---
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            PlayerStats attackerStats = statsManager.getPlayerStats(attacker);
            if (attackerStats != null) {
                double bonusDamage = attackerStats.getStrength() * damagePerStrength;
                event.setDamage(event.getDamage() + bonusDamage);
            }
        }
    }
}
