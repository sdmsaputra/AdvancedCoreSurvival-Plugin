package com.minekarta.advancedcoresurvival.modules.rpg.skills;

import org.bukkit.Material;
import java.util.List;

/**
 * A data class representing a single skill from the configuration.
 * This object is immutable and holds all defined properties of a skill.
 */
public class Skill {

    private final String id;
    private final boolean enabled;
    private final String name;
    private final List<String> description;
    private final Material icon;
    private final int cost;
    private final int maxLevel;
    private final String type;
    private final String stat;
    private final double value;
    private final List<String> requirements;

    public Skill(String id, boolean enabled, String name, List<String> description, Material icon, int cost, int maxLevel, String type, String stat, double value, List<String> requirements) {
        this.id = id;
        this.enabled = enabled;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.cost = cost;
        this.maxLevel = maxLevel;
        this.type = type;
        this.stat = stat;
        this.value = value;
        this.requirements = requirements;
    }

    // --- Getters ---

    public String getId() { return id; }
    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }
    public List<String> getDescription() { return description; }
    public Material getIcon() { return icon; }
    public int getCost() { return cost; }
    public int getMaxLevel() { return maxLevel; }
    public String getType() { return type; }
    public String getStat() { return stat; }
    public double getValue() { return value; }
    public List<String> getRequirements() { return requirements; }
}
