package com.minekarta.advancedcoresurvival.modules.mmorpg.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.modules.mmorpg.ClassManager;
import com.minekarta.advancedcoresurvival.modules.mmorpg.PlayerClass;
import com.minekarta.advancedcoresurvival.modules.mmorpg.ui.ClassSelectionGUI;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClassCommand implements CommandExecutor {

    private final AdvancedCoreSurvival plugin;
    private final PlayerStatsManager statsManager;
    private final ClassManager classManager;

    public ClassCommand(AdvancedCoreSurvival plugin, PlayerStatsManager statsManager, ClassManager classManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.classManager = classManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        String playerClassId = statsManager.getPlayerStats(player).getPlayerClass();
        boolean allowReclass = plugin.getConfig().getBoolean("mmorpg.allow-reclass", false);

        if (playerClassId == null || allowReclass) {
            new ClassSelectionGUI(player, plugin, statsManager, classManager).open();
        } else {
            PlayerClass currentClass = classManager.getClass(playerClassId);
            if (currentClass != null) {
                player.sendMessage(Component.text("You are a " + currentClass.getName()).color(NamedTextColor.GOLD));
                // Later, add more info like abilities
            } else {
                 player.sendMessage(Component.text("You don't have a class yet. Use /class to choose one.").color(NamedTextColor.YELLOW));
            }
        }
        return true;
    }
}
