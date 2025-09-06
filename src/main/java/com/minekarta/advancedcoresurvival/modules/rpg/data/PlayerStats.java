package com.minekarta.advancedcoresurvival.modules.rpg.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A data class to hold all RPG-related statistics for a single player.
 */
public class PlayerStats {

    private final UUID playerUUID;
    private int level;
    private double exp;

    // Core player attributes
    private int strength;
    private int agility;
    private int endurance;
    private int skillPoints;

    // Player skills, represented as a map of skill name to skill level
    private final Map<String, Integer> skillLevels;

    // A map to store calculated bonuses from skills, e.g., "SWORD_DAMAGE_BONUS" -> 0.1
    private final Map<String, Double> statBonuses;

    /**
     * Constructor for a new player's stats.
     * Initializes all stats and skills to default values.
     * @param playerUUID The UUID of the player.
     */
    public PlayerStats(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.level = 1;
        this.exp = 0;
        this.strength = 5;
        this.agility = 5;
        this.endurance = 5;
        this.skillPoints = 0;
        this.skillLevels = new HashMap<>();
        this.statBonuses = new HashMap<>();
        // Initialize default skills
        this.skillLevels.put("MINING", 1);
        this.skillLevels.put("FARMING", 1);
        this.skillLevels.put("WOODCUTTING", 1);
    }

    // --- Bonus Management ---

    /**
     * Gets the calculated bonus value for a specific stat.
     * @param stat The name of the stat bonus (e.g., "SWORD_DAMAGE_BONUS").
     * @return The total bonus value, or 0.0 if none.
     */
    public double getStatBonus(String stat) {
        return statBonuses.getOrDefault(stat.toUpperCase(), 0.0);
    }

    /**
     * Recalculates all stat bonuses based on the player's current skill levels.
     * This should be called whenever a skill level changes.
     * @param skillManager The manager that holds all skill definitions.
     */
    public void recalculateStatBonuses(com.minekarta.advancedcoresurvival.modules.rpg.skills.SkillManager skillManager) {
        statBonuses.clear();
        if (skillManager == null) return;

        for (Map.Entry<String, Integer> entry : skillLevels.entrySet()) {
            String skillId = entry.getKey();
            int level = entry.getValue();
            com.minekarta.advancedcoresurvival.modules.rpg.skills.Skill skill = skillManager.getSkill(skillId);

            if (skill != null && skill.getType().equalsIgnoreCase("PASSIVE")) {
                double currentBonus = statBonuses.getOrDefault(skill.getStat(), 0.0);
                statBonuses.put(skill.getStat(), currentBonus + (skill.getValue() * level));
            }
        }
    }


    // --- Getters and Setters ---

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getExp() {
        return exp;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public void addExp(double amount) {
        this.exp += amount;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getAgility() {
        return agility;
    }

    public void setAgility(int agility) {
        this.agility = agility;
    }

    public int getEndurance() {
        return endurance;
    }

    public void setEndurance(int endurance) {
        this.endurance = endurance;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public void addSkillPoints(int amount) {
        this.skillPoints += amount;
    }

    public Map<String, Integer> getSkillLevels() {
        return skillLevels;
    }

    public int getSkillLevel(String skillName) {
        return skillLevels.getOrDefault(skillName.toUpperCase(), 0);
    }

    public void setSkillLevel(String skillName, int level) {
        skillLevels.put(skillName.toUpperCase(), level);
    }
}
