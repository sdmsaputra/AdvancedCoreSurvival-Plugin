package com.minekarta.advancedcoresurvival.modules.essentials.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.storage.StorageManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    private final AdvancedCoreSurvival plugin;
    private final StorageManager storageManager;

    public SpawnCommand(AdvancedCoreSurvival plugin) {
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
        if (!player.hasPermission("advancedcoresurvival.essentials.spawn")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        storageManager.getStorage().getSpawnLocation().thenAccept(spawnLocation -> {
            // This part runs on the main server thread after the async operation is complete
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (spawnLocation != null) {
                    player.teleport(spawnLocation);
                    player.sendMessage(ChatColor.GREEN + "Teleporting you to the spawn...");
                } else {
                    player.sendMessage(ChatColor.RED + "The server spawn has not been set yet. Contact an administrator.");
                }
            });
        });

        return true;
    }
}
