package com.minekarta.advancedcoresurvival.modules.mmorpg;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ClassManager {

    private final Map<String, PlayerClass> classes = new HashMap<>();
    private final AdvancedCoreSurvival plugin;

    public ClassManager(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void loadClasses() {
        classes.clear();
        ConfigurationSection classConfig = plugin.getConfig().getConfigurationSection("mmorpg.classes");
        if (classConfig == null) {
            plugin.getLogger().info("No 'mmorpg.classes' section found in config.yml. No classes will be loaded.");
            return;
        }

        for (String classId : classConfig.getKeys(false)) {
            ConfigurationSection section = classConfig.getConfigurationSection(classId);
            if (section == null) continue;

            try {
                String name = section.getString("name", "Unnamed Class");
                List<String> description = section.getStringList("description");
                Material icon = Material.matchMaterial(section.getString("icon", "BARRIER"));
                if (icon == null) {
                    plugin.getLogger().warning("Invalid icon material for class '" + classId + "'. Defaulting to BARRIER.");
                    icon = Material.BARRIER;
                }

                Map<String, Integer> baseStats = new HashMap<>();
                ConfigurationSection statsSection = section.getConfigurationSection("base-stats");
                if (statsSection != null) {
                    for (String statName : statsSection.getKeys(false)) {
                        baseStats.put(statName.toLowerCase(), statsSection.getInt(statName));
                    }
                }

                double baseMana = section.getDouble("base-mana", 100.0);
                List<String> abilities = section.getStringList("abilities");

                PlayerClass playerClass = new PlayerClass(classId, name, icon, description, baseStats, baseMana, abilities);
                classes.put(classId.toLowerCase(), playerClass);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load class '" + classId + "'. Please check its configuration.", e);
            }
        }
        plugin.getLogger().info("Successfully loaded " + classes.size() + " classes.");
    }

    public PlayerClass getClass(String id) {
        return classes.get(id.toLowerCase());
    }

    public Map<String, PlayerClass> getClasses() {
        return Collections.unmodifiableMap(classes);
    }
}
