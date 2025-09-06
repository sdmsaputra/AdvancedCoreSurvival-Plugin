package com.minekarta.advancedcoresurvival.modules.rpg.commands;

import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import com.minekarta.advancedcoresurvival.modules.rpg.skills.SkillManager;
import com.minekarta.advancedcoresurvival.modules.rpg.ui.SkillTreeGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RPGCommand implements CommandExecutor {

    private final PlayerStatsManager statsManager;
    private final SkillManager skillManager;

    public RPGCommand(PlayerStatsManager statsManager, SkillManager skillManager) {
        this.statsManager = statsManager;
        this.skillManager = skillManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by a player.").color(NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("skills"))) {
            openSkillsGUI(player);
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("stats")) {
            showStats(player);
            return true;
        }

        player.sendMessage(Component.text("Usage: /rpg [skills|stats]").color(NamedTextColor.GRAY));
        return true;
    }

    private void openSkillsGUI(Player player) {
        new SkillTreeGUI(player, statsManager, skillManager).open();
    }

    private void showStats(Player player) {
        PlayerStats stats = statsManager.getPlayerStats(player);
        player.sendMessage(Component.text("--- Your Stats ---").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("Level: ").color(NamedTextColor.GRAY).append(Component.text(stats.getLevel(), NamedTextColor.WHITE)));
        player.sendMessage(Component.text("EXP: ").color(NamedTextColor.GRAY).append(Component.text(String.format("%.2f", stats.getExp()), NamedTextColor.WHITE)));
        player.sendMessage(Component.text("Skill Points: ").color(NamedTextColor.GRAY).append(Component.text(stats.getSkillPoints(), NamedTextColor.AQUA)));
        player.sendMessage(Component.text("----------------").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("Strength: ").color(NamedTextColor.RED).append(Component.text(stats.getStrength())));
        player.sendMessage(Component.text("Agility: ").color(NamedTextColor.GREEN).append(Component.text(stats.getAgility())));
        player.sendMessage(Component.text("Endurance: ").color(NamedTextColor.BLUE).append(Component.text(stats.getEndurance())));
        player.sendMessage(Component.text("----------------").color(NamedTextColor.GOLD));
    }
}
