package com.minekarta.advancedcoresurvival.modules.essentials.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.core.storage.StorageManager;
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
            sender.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.must-be-player"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("advancedcoresurvival.essentials.spawn")) {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.no-permission"));
            return true;
        }

        storageManager.getStorage().getSpawnLocation().thenAccept(spawnLocation -> {
            // This part runs on the main server thread after the async operation is complete
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (spawnLocation != null) {
                    player.teleport(spawnLocation);
                    player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.spawn.teleporting"));
                } else {
                    player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.spawn.not-set"));
                }
            });
        });

        return true;
    }
}
