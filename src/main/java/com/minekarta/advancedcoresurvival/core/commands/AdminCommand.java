package com.minekarta.advancedcoresurvival.core.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.modules.ModuleManager;
import com.minekarta.advancedcoresurvival.modules.claims.ClaimsModule;
import com.minekarta.advancedcoresurvival.modules.claims.tax.ClaimTaxManager;
import com.minekarta.advancedcoresurvival.modules.rpg.RPGModule;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AdminCommand implements CommandExecutor {

    private final AdvancedCoreSurvival plugin;

    public AdminCommand(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("AdvancedCoreSurvival Admin").color(NamedTextColor.GOLD));
            sender.sendMessage(Component.text("/acs runtax - Manually run the claim tax collection.").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("/acs stats <player> <stat> <value> - Set a player's stat.").color(NamedTextColor.GRAY));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "runtax":
                handleRunTax(sender);
                return true;
            case "stats":
                handleStats(sender, args);
                return true;
        }

        return true;
    }

    private void handleStats(CommandSender sender, String[] args) {
        if (!sender.hasPermission("advancedcoresurvival.admin")) {
            sender.sendMessage(Component.text("You don't have permission to do that.").color(NamedTextColor.RED));
            return;
        }

        if (args.length != 4) {
            sender.sendMessage(Component.text("Usage: /acs stats <player> <stat> <value>").color(NamedTextColor.RED));
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        String statName = args[2].toLowerCase();
        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Value must be a number.").color(NamedTextColor.RED));
            return;
        }

        plugin.getModuleManager().getModule("rpg").ifPresentOrElse(module -> {
            RPGModule rpgModule = (RPGModule) module;
            PlayerStatsManager statsManager = rpgModule.getStatsManager();
            PlayerStats stats = statsManager.getPlayerStats(player.getUniqueId());

            boolean statChanged = false;
            switch (statName) {
                case "endurance":
                    stats.setEndurance(value);
                    statChanged = true;
                    break;
                // Add other stats here in the future
                default:
                    sender.sendMessage(Component.text("Unknown stat: " + statName).color(NamedTextColor.RED));
                    return;
            }

            if (statChanged) {
                statsManager.savePlayerStats(player.getUniqueId());
                if (player.isOnline()) {
                    statsManager.applyAllBonuses(player.getPlayer());
                }
                sender.sendMessage(Component.text(player.getName() + "'s " + statName + " set to " + value + ".").color(NamedTextColor.GREEN));
            }
        }, () -> {
            sender.sendMessage(Component.text("RPG module is not enabled.").color(NamedTextColor.RED));
        });
    }

    private void handleRunTax(CommandSender sender) {
        if (!sender.hasPermission("advancedcoresurvival.admin")) {
            sender.sendMessage(Component.text("You don't have permission to do that.").color(NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Manually starting claim tax collection...").color(NamedTextColor.YELLOW));

        // Get the ClaimsModule and its TaxManager directly
        plugin.getModuleManager().getModule("claims").ifPresentOrElse(module -> {
            ClaimsModule claimsModule = (ClaimsModule) module;
            ClaimTaxManager taxManager = claimsModule.getTaxManager();

            if (taxManager != null) {
                taxManager.collectTaxes();
                sender.sendMessage(Component.text("Tax collection process started asynchronously. Check console for details.").color(NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("ClaimTaxManager is not available.").color(NamedTextColor.RED));
            }
        }, () -> {
            sender.sendMessage(Component.text("Claims module is not enabled.").color(NamedTextColor.RED));
        });
    }
}
