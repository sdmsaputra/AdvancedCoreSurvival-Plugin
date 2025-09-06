package com.minekarta.advancedcoresurvival.modules.rpg.skills;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages the loading and retrieval of all available skills from the configuration.
 */
public class SkillManager {

    private final Map<String, Skill> skills = new HashMap<>();
    private final AdvancedCoreSurvival plugin;

    public SkillManager(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads all skills from the 'rpg.skill-tree' section of the config.
     */
    public void loadSkills() {
        skills.clear();
        ConfigurationSection skillConfig = plugin.getConfig().getConfigurationSection("rpg.skill-tree");
        if (skillConfig == null) {
            plugin.getLogger().info("No 'skill-tree' section found in config.yml. No skills will be loaded.");
            return;
        }

        for (String skillId : skillConfig.getKeys(false)) {
            ConfigurationSection section = skillConfig.getConfigurationSection(skillId);
            if (section == null) continue;

            try {
                boolean enabled = section.getBoolean("enabled", false);
                if (!enabled) {
                    plugin.getLogger().fine("Skill '" + skillId + "' is disabled in config, skipping.");
                    continue;
                }

                String name = section.getString("name", "Unnamed Skill");
                List<String> description = section.getStringList("description");
                Material icon = Material.matchMaterial(section.getString("icon", "BARRIER"));
                if (icon == null) {
                    plugin.getLogger().warning("Invalid icon material for skill '" + skillId + "'. Defaulting to BARRIER.");
                    icon = Material.BARRIER;
                }
                int cost = section.getInt("cost", 1);
                int maxLevel = section.getInt("max-level", 5);
                String type = section.getString("type", "PASSIVE");
                String stat = section.getString("stat", "");
                double value = section.getDouble("value", 0.0);
                List<String> requirements = section.getStringList("requires");

                Skill skill = new Skill(skillId, enabled, name, description, icon, cost, maxLevel, type, stat, value, requirements);
                skills.put(skillId.toLowerCase(), skill);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load skill '" + skillId + "'. Please check its configuration.", e);
            }
        }
        plugin.getLogger().info("Successfully loaded " + skills.size() + " skills.");
    }

    public Skill getSkill(String id) {
        return skills.get(id.toLowerCase());
    }

    public Map<String, Skill> getSkills() {
        return Collections.unmodifiableMap(skills);
    }
}
