package com.minekarta.advancedcoresurvival.core.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.core.modules.ModuleManager;
import com.minekarta.advancedcoresurvival.modules.claims.ClaimsModule;
import com.minekarta.advancedcoresurvival.modules.claims.tax.ClaimTaxManager;
import com.minekarta.advancedcoresurvival.modules.rpg.RPGModule;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
            sender.sendMessage(l("admin.help.header"));
            sender.sendMessage(l("admin.help.runtax"));
            sender.sendMessage(l("admin.help.stats"));
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
            sender.sendMessage(l("general.no-permission"));
            return;
        }

        if (args.length != 4) {
            sender.sendMessage(l("admin.stats.usage"));
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        String statName = args[2].toLowerCase();
        int value;
        try {
            value = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(l("admin.stats.nan"));
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
                    sender.sendMessage(l("admin.stats.unknown-stat", "%stat%", statName));
                    return;
            }

            if (statChanged) {
                statsManager.savePlayerStats(player.getUniqueId());
                if (player.isOnline()) {
                    statsManager.applyAllBonuses(player.getPlayer());
                }
                sender.sendMessage(l("admin.stats.success", "%player%", player.getName(), "%stat%", statName, "%value%", String.valueOf(value)));
            }
        }, () -> {
            sender.sendMessage(l("admin.module-not-enabled", "%module%", "RPG"));
        });
    }

    private void handleRunTax(CommandSender sender) {
        if (!sender.hasPermission("advancedcoresurvival.admin")) {
            sender.sendMessage(l("general.no-permission"));
            return;
        }

        sender.sendMessage(l("admin.runtax.starting"));

        // Get the ClaimsModule and its TaxManager directly
        plugin.getModuleManager().getModule("claims").ifPresentOrElse(module -> {
            ClaimsModule claimsModule = (ClaimsModule) module;
            ClaimTaxManager taxManager = claimsModule.getTaxManager();

            if (taxManager != null) {
                taxManager.collectTaxes();
                sender.sendMessage(l("admin.runtax.success"));
            } else {
                sender.sendMessage(l("admin.runtax.no-manager"));
            }
        }, () -> {
            sender.sendMessage(l("admin.module-not-enabled", "%module%", "Claims"));
        });
    }

    /**
     * Helper method to shorten the LocaleManager call.
     */
    private Component l(String key, String... replacements) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(LocaleManager.getInstance().getFormattedMessage(key, replacements));
    }
}
