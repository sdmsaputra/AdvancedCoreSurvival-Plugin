package com.minekarta.advancedcoresurvival.modules.economy.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.NumberFormat;

public class BalanceCommand implements CommandExecutor {

    private final AdvancedCoreSurvival plugin;

    public BalanceCommand(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("advancedcoresurvival.economy.balance")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: /balance <player>");
                return true;
            }
            Player player = (Player) sender;
            showBalance(player, player);
        } else {
            if (!sender.hasPermission("advancedcoresurvival.economy.balance.others")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to check other players' balances.");
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null || !target.hasPlayedBefore()) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
            showBalance(sender, target);
        }
        return true;
    }

    private void showBalance(CommandSender sender, OfflinePlayer target) {
        plugin.getStorageManager().getStorage().getPlayerBalance(target.getUniqueId()).thenAccept(balance -> {
            String currencySymbol = plugin.getConfigManager().getCurrencySymbol();
            String formattedBalance = NumberFormat.getCurrencyInstance().format(balance).replace("$", currencySymbol);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (sender == target) {
                    sender.sendMessage(ChatColor.GREEN + "Your balance: " + ChatColor.WHITE + formattedBalance);
                } else {
                    sender.sendMessage(ChatColor.GREEN + target.getName() + "'s balance: " + ChatColor.WHITE + formattedBalance);
                }
            });
        });
    }
}
