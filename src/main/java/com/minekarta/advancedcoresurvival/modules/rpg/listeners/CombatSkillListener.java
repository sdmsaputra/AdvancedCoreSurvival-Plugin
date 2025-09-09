package com.minekarta.advancedcoresurvival.modules.rpg.listeners;

import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import com.minekarta.advancedcoresurvival.modules.rpg.leveling.LevelingManager;
import org.bukkit.Sound;
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
                    // Send dodge feedback
                    String dodgeMessage = LocaleManager.getInstance().getFormattedMessage("rpg.dodge-message");
                    if (!dodgeMessage.isEmpty()) {
                        victim.sendMessage(dodgeMessage);
                    }
                    victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
                    return; // Stop processing damage if dodged
                }
            }
        }

        // --- Strength (Damage Bonus) & Skill-based Damage Bonus ---
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            PlayerStats attackerStats = statsManager.getPlayerStats(attacker);
            if (attackerStats != null) {
                // Base bonus damage from Strength (flat increase)
                double bonusDamage = attackerStats.getStrength() * damagePerStrength;
                double currentDamage = event.getDamage() + bonusDamage;

                // Multiplicative bonus from skills (e.g., SWORD_DAMAGE_BONUS)
                double damageMultiplier = 1.0;
                String itemInHand = attacker.getInventory().getItemInMainHand().getType().name();
                if (itemInHand.endsWith("_SWORD")) {
                    damageMultiplier += attackerStats.getStatBonus("SWORD_DAMAGE_BONUS");
                }

                event.setDamage(currentDamage * damageMultiplier);
            }
        }
    }
}
