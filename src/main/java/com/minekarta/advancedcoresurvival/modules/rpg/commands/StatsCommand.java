package com.minekarta.advancedcoresurvival.modules.rpg.commands;

import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {

    private final PlayerStatsManager statsManager;

    public StatsCommand(PlayerStatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        PlayerStats stats = statsManager.getPlayerStats(player);

        if (stats == null) {
            player.sendMessage(ChatColor.RED + "Your stats could not be loaded. Please try relogging.");
            return true;
        }

        // Format and send the stats message to the player
        player.sendMessage(ChatColor.GOLD + "--- Your Stats ---");
        player.sendMessage(ChatColor.YELLOW + "Level: " + ChatColor.WHITE + stats.getLevel());
        player.sendMessage(ChatColor.YELLOW + "EXP: " + ChatColor.WHITE + String.format("%.2f", stats.getExp()));
        player.sendMessage("");
        player.sendMessage(ChatColor.AQUA + "Attributes:");
        player.sendMessage(ChatColor.GRAY + " - Strength: " + ChatColor.WHITE + stats.getStrength());
        player.sendMessage(ChatColor.GRAY + " - Agility: " + ChatColor.WHITE + stats.getAgility());
        player.sendMessage(ChatColor.GRAY + " - Endurance: " + ChatColor.WHITE + stats.getEndurance());
        player.sendMessage("");
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Skill Points: " + ChatColor.WHITE + stats.getSkillPoints());
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "Skills:");
        stats.getSkillLevels().forEach((skill, level) -> {
            player.sendMessage(ChatColor.GRAY + " - " + skill + ": " + ChatColor.WHITE + level);
        });
        player.sendMessage(ChatColor.GOLD + "------------------");

        return true;
    }
}
