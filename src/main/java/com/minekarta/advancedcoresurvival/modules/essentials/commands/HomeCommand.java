package com.minekarta.advancedcoresurvival.modules.essentials.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.storage.StorageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    private final AdvancedCoreSurvival plugin;
    private final StorageManager storageManager;

    public HomeCommand(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("advancedcoresurvival.essentials.home")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            // List homes
            storageManager.getStorage().listHomes(player.getUniqueId()).thenAccept(homeList -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (homeList.isEmpty()) {
                        player.sendMessage(ChatColor.YELLOW + "You have no homes set. Use /sethome <name> to set one.");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "Your homes: " + ChatColor.WHITE + String.join(", ", homeList));
                    }
                });
            });
            return true;
        }

        String homeName = args[0].toLowerCase();
        storageManager.getStorage().getHome(player.getUniqueId(), homeName).thenAccept(location -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (location != null) {
                    player.teleport(location);
                    player.sendMessage(ChatColor.GREEN + "Teleporting to home '" + homeName + "'.");
                } else {
                    player.sendMessage(ChatColor.RED + "Home '" + homeName + "' not found.");
                }
            });
        });

        return true;
    }
}
