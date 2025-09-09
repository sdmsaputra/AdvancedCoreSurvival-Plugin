package com.minekarta.advancedcoresurvival.modules.essentials.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.core.storage.StorageManager;
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
            sender.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.must-be-player"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("advancedcoresurvival.essentials.home")) {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            // List homes
            storageManager.getStorage().listHomes(player.getUniqueId()).thenAccept(homeList -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (homeList.isEmpty()) {
                        player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.home.no-homes"));
                    } else {
                        player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.home.list", "%homelist%", String.join(", ", homeList)));
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
                    player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.home.teleporting", "%home%", homeName));
                } else {
                    player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.home.not-found", "%home%", homeName));
                }
            });
        });

        return true;
    }
}
