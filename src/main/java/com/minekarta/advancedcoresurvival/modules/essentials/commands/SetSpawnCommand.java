package com.minekarta.advancedcoresurvival.modules.essentials.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.core.storage.StorageManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {

    private final AdvancedCoreSurvival plugin;
    private final StorageManager storageManager;

    public SetSpawnCommand(AdvancedCoreSurvival plugin) {
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
        if (!player.hasPermission("advancedcoresurvival.essentials.setspawn")) {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.no-permission"));
            return true;
        }

        Location spawnLocation = player.getLocation();

        storageManager.getStorage().setSpawnLocation(spawnLocation).thenRun(() -> {
            // This part runs on the main server thread after the async operation is complete
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.spawn.set-success"));
            });
        });

        return true;
    }
}
