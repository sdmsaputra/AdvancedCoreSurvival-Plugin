package com.minekarta.advancedcoresurvival.modules.rpg;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.Module;
import com.minekarta.advancedcoresurvival.core.storage.Storage;
import com.minekarta.advancedcoresurvival.modules.rpg.commands.RPGCommand;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import com.minekarta.advancedcoresurvival.modules.rpg.leveling.LevelingManager;
import com.minekarta.advancedcoresurvival.modules.rpg.listeners.*;
import com.minekarta.advancedcoresurvival.modules.rpg.skills.SkillManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;

public class RPGModule implements Module {

    private PlayerStatsManager statsManager;
    private LevelingManager levelingManager;
    private SkillManager skillManager;

    @Override
    public String getName() {
        return "rpg";
    }

    @Override
    public void onEnable(AdvancedCoreSurvival plugin) {
        plugin.getLogger().info("Initializing RPG Module...");

        // Get storage manager
        Storage storage = plugin.getStorageManager().getStorage();

        // Initialize managers
        this.statsManager = new PlayerStatsManager(plugin.getConfig(), storage);
        this.skillManager = new SkillManager(plugin);
        this.levelingManager = new LevelingManager(statsManager, plugin.getConfig());

        // Load data from config
        skillManager.loadSkills();

        // Register Listeners
        plugin.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(statsManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MiningSkillListener(levelingManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new FarmingSkillListener(levelingManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new WoodcuttingSkillListener(levelingManager), plugin);
        registerCombatListener(plugin);

        // Register Commands
        plugin.getCommand("rpg").setExecutor(new RPGCommand(statsManager, skillManager));

        // Load stats for any players who are already online (e.g., on a /reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            statsManager.getPlayerStats(player);
        }
    }

    public PlayerStatsManager getStatsManager() {
        return statsManager;
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

    private void registerCombatListener(AdvancedCoreSurvival plugin) {
        // Load combat EXP values
        Map<EntityType, Double> combatExpValues = new EnumMap<>(EntityType.class);
        ConfigurationSection combatSection = plugin.getConfig().getConfigurationSection("rpg.exp-rewards.combat");
        if (combatSection != null) {
            for (String entityName : combatSection.getKeys(false)) {
                try {
                    EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                    double expValue = combatSection.getDouble(entityName);
                    combatExpValues.put(entityType, expValue);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[RPG] Invalid entity type in config.yml: " + entityName);
                }
            }
        }

        // Load stat effect values from config
        double damagePerStrength = plugin.getConfig().getDouble("rpg.stats.damage-per-strength", 0.5);
        double dodgeChancePerAgility = plugin.getConfig().getDouble("rpg.stats.dodge-chance-per-agility", 0.005);

        // Register the listener with all required dependencies
        CombatSkillListener combatListener = new CombatSkillListener(
                levelingManager,
                statsManager,
                combatExpValues,
                damagePerStrength,
                dodgeChancePerAgility
        );
        plugin.getServer().getPluginManager().registerEvents(combatListener, plugin);
        plugin.getLogger().info("Combat listener registered with stat effects enabled.");
    }
}
