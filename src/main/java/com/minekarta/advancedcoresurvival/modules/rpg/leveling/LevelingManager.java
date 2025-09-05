package com.minekarta.advancedcoresurvival.modules.rpg.leveling;

import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class LevelingManager {

    private final PlayerStatsManager statsManager;
    private final double baseExp;
    private final double expMultiplier;
    private final int skillPointsPerLevel;
    private final String levelUpMessage;
    private final String expGainActionBar;

    public LevelingManager(PlayerStatsManager statsManager, FileConfiguration config) {
        this.statsManager = statsManager;

        // Load settings from config
        this.baseExp = config.getDouble("rpg.leveling.base-exp", 1000.0);
        this.expMultiplier = config.getDouble("rpg.leveling.exp-multiplier", 1.2);
        this.skillPointsPerLevel = config.getInt("rpg.leveling.skill-points-per-level", 1);
        this.levelUpMessage = config.getString("rpg.leveling.level-up-message", "&a&lCongratulations, %player_name%! You have reached Level %new_level%!");
        this.expGainActionBar = config.getString("rpg.leveling.exp-gain-action-bar", "&b+%gained_exp% EXP &7[&a%exp_bar%&7] &e%current_exp%&7/&e%required_exp%");
    }

    /**
     * Calculates the total EXP required to reach a certain level.
     * @param level The level to calculate the required EXP for.
     * @return The total EXP required.
     */
    public double getRequiredExp(int level) {
        if (level <= 1) {
            return 0;
        }
        // Formula: base * (multiplier ^ (level - 2))
        // We use level - 2 because level 2 should use the base_exp (multiplier^0)
        return baseExp * Math.pow(expMultiplier, level - 2);
    }

    /**
     * Checks if a player has enough EXP to level up and processes the level up if they do.
     * This can handle multiple level-ups in one go.
     * @param player The player to check.
     */
    public void checkAndPromote(Player player) {
        PlayerStats stats = statsManager.getPlayerStats(player);
        if (stats == null) return;

        double requiredExp = getRequiredExp(stats.getLevel() + 1);

        // Loop in case they gain enough EXP for multiple levels
        while (stats.getExp() >= requiredExp) {
            // Level up!
            stats.setLevel(stats.getLevel() + 1);
            stats.addSkillPoints(skillPointsPerLevel);
            stats.setExp(stats.getExp() - requiredExp);

            // Send level up message
            if (levelUpMessage != null && !levelUpMessage.isEmpty()) {
                String message = ChatColor.translateAlternateColorCodes('&', levelUpMessage
                        .replace("%player_name%", player.getName())
                        .replace("%new_level%", String.valueOf(stats.getLevel())));
                player.sendMessage(message);
            }

            // TODO: Add sound effects or other rewards here in the future.

            // Get the requirement for the next level
            requiredExp = getRequiredExp(stats.getLevel() + 1);
        }
    }

    /**
     * Call this method after adding EXP to a player.
     * It will check for level up and send an action bar message.
     * @param player The player who gained EXP.
     * @param amountGained The amount of EXP they just gained.
     */
    public void onExpGain(Player player, double amountGained) {
        PlayerStats stats = statsManager.getPlayerStats(player);
        if (stats == null) return;

        // Add the experience
        stats.addExp(amountGained);

        // Check for level up
        checkAndPromote(player);

        // Send action bar message
        sendExpActionBar(player, amountGained);
    }

    /**
     * Sends the EXP gain action bar message to the player.
     * @param player The player to send the message to.
     * @param gainedAmount The amount of EXP the player just gained.
     */
    public void sendExpActionBar(Player player, double gainedAmount) {
        if (expGainActionBar == null || expGainActionBar.isEmpty()) {
            return;
        }

        PlayerStats stats = statsManager.getPlayerStats(player);
        if (stats == null) return;

        double currentExp = stats.getExp();
        double requiredExp = getRequiredExp(stats.getLevel() + 1);

        String message = expGainActionBar
                .replace("%gained_exp%", String.format("%.1f", gainedAmount))
                .replace("%current_exp%", String.format("%.1f", currentExp))
                .replace("%required_exp%", String.format("%.0f", requiredExp));

        // Create the EXP bar
        message = message.replace("%exp_bar%", createProgressBar(currentExp, requiredExp));

        // Using spigot API to send action bar messages
        // Note: This needs to be compiled against Spigot/Paper API
        // For now, let's just send it as a regular message for compatibility.
        // In a real scenario, you would use Adventure or Spigot's Chat API.
        player.sendActionBar(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }

    /**
     * Creates a textual progress bar.
     * @param current The current value.
     * @param max The maximum value.
     * @return A string representing the progress bar.
     */
    private String createProgressBar(double current, double max) {
        if (max <= 0) return "-------";

        int barLength = 10; // The total number of characters in the bar
        int progress = (int) ((current / max) * barLength);

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                bar.append(ChatColor.GREEN + "|");
            } else {
                bar.append(ChatColor.GRAY + "|");
            }
        }
        return bar.toString();
    }
}
