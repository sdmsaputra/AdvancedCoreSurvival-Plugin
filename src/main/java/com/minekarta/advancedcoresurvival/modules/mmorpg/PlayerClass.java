package com.minekarta.advancedcoresurvival.modules.mmorpg;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;

/**
 * A data class representing a single character class from the configuration.
 */
public class PlayerClass {

    private final String id;
    private final String name;
    private final Material icon;
    private final List<String> description;
    private final Map<String, Integer> baseStats;
    private final double baseMana;
    private final List<String> abilities;

    public PlayerClass(String id, String name, Material icon, List<String> description, Map<String, Integer> baseStats, double baseMana, List<String> abilities) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.baseStats = baseStats;
        this.baseMana = baseMana;
        this.abilities = abilities;
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public List<String> getDescription() {
        return description;
    }

    public Map<String, Integer> getBaseStats() {
        return baseStats;
    }

    public double getBaseMana() {
        return baseMana;
    }

    public List<String> getAbilities() {
        return abilities;
    }
}
