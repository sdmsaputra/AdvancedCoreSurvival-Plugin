package com.minekarta.advancedcoresurvival.modules.mmorpg.ability;

/**
 * A data class representing a single ability from the configuration.
 */
public class Ability {

    private final String id;
    private final String name;
    private final String type;
    private final double manaCost;
    private final int cooldown; // in seconds
    private final String description;

    public Ability(String id, String name, String type, double manaCost, int cooldown, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.description = description;
    }

    // --- Getters ---

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getManaCost() {
        return manaCost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public String getDescription() {
        return description;
    }
}
