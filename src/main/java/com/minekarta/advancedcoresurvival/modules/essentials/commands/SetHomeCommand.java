package com.minekarta.advancedcoresurvival.modules.essentials.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.config.ConfigManager;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.core.storage.StorageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {

    private final AdvancedCoreSurvival plugin;
    private final StorageManager storageManager;
    private final ConfigManager configManager;

    public SetHomeCommand(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
        this.storageManager = plugin.getStorageManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.must-be-player"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("advancedcoresurvival.essentials.sethome")) {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.no-permission"));
            return true;
        }

        String homeName = "home"; // Default home name
        if (args.length > 0) {
            homeName = args[0].toLowerCase();
        }

        if (!homeName.matches("^[a-zA-Z0-9]+$")) {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.home.name-invalid"));
            return true;
        }

        final String finalHomeName = homeName;

        // Asynchronous check for home count
        storageManager.getStorage().getHomeCount(player.getUniqueId()).thenAccept(homeCount -> {
            int maxHomes = configManager.getMaxHomes();
            // Note: This doesn't account for replacing an existing home. A more robust check would be needed.
            // For now, we assume setting a home always adds to the count for simplicity.
            if (homeCount >= maxHomes) {
                // Schedule sync task to send message
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.home.limit-reached", "%limit%", String.valueOf(maxHomes)));
                });
                return;
            }

            // Set the home
            storageManager.getStorage().setHome(player.getUniqueId(), finalHomeName, player.getLocation()).thenRun(() -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(LocaleManager.getInstance().getFormattedMessage("essentials.home.set-success", "%home%", finalHomeName));
                });
            });
        });

        return true;
    }
}
