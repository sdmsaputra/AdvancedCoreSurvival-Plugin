package com.minekarta.advancedcoresurvival.modules.mmorpg.ability;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AbilityManager {

    private final Map<String, Ability> abilities = new HashMap<>();
    private final AdvancedCoreSurvival plugin;

    public AbilityManager(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void loadAbilities() {
        abilities.clear();
        ConfigurationSection abilityConfig = plugin.getConfig().getConfigurationSection("abilities");
        if (abilityConfig == null) {
            plugin.getLogger().info("No 'abilities' section found in config.yml. No abilities will be loaded.");
            return;
        }

        for (String abilityId : abilityConfig.getKeys(false)) {
            ConfigurationSection section = abilityConfig.getConfigurationSection(abilityId);
            if (section == null) continue;

            try {
                String name = section.getString("name", "Unnamed Ability");
                String type = section.getString("type", "ACTIVE");
                double manaCost = section.getDouble("mana-cost", 0.0);
                int cooldown = section.getInt("cooldown", 0);
                String description = section.getString("description", "");

                Ability ability = new Ability(abilityId, name, type, manaCost, cooldown, description);
                abilities.put(abilityId.toLowerCase(), ability);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load ability '" + abilityId + "'. Please check its configuration.", e);
            }
        }
        plugin.getLogger().info("Successfully loaded " + abilities.size() + " abilities.");
    }

    public Ability getAbility(String id) {
        return abilities.get(id.toLowerCase());
    }

    public Map<String, Ability> getAbilities() {
        return Collections.unmodifiableMap(abilities);
    }
}
