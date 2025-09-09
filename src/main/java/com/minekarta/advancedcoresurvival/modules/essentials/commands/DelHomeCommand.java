package com.minekarta.advancedcoresurvival.modules.essentials.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.core.storage.StorageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelHomeCommand implements CommandExecutor {

    private final AdvancedCoreSurvival plugin;
    private final StorageManager storageManager;

    public DelHomeCommand(AdvancedCoreSurvival plugin) {
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
        if (!player.hasPermission("advancedcoresurvival.essentials.delhome")) {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.invalid-usage", "%usage%", "/delhome <name>"));
            return true;
        }

        String homeName = args[0].toLowerCase();

        // Check if the home exists before trying to delete it
        storageManager.getStorage().getHome(player.getUniqueId(), homeName).thenAccept(location -> {
            if (location == null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.home.not-found", "%home%", homeName));
                });
                return;
            }

            // Home exists, so delete it
            storageManager.getStorage().deleteHome(player.getUniqueId(), homeName).thenRun(() -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.home.deleted", "%home%", homeName));
                });
            });
        });

        return true;
    }
}
