package com.minekarta.advancedcoresurvival.modules.claims.commands;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand implements CommandExecutor {

    private final AdvancedCoreSurvival plugin;

    public ClaimCommand(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.must-be-player"));
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            // Show help message
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.invalid-usage", "%usage%", "/claim <create|delete|trust|untrust|info>"));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                handleCreate(player);
                break;
            // Other cases for delete, trust, etc. will be added later
            default:
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.invalid-usage", "%usage%", "/claim <create|delete|trust|untrust|info>"));
                break;
        }
        return true;
    }

    private void handleCreate(Player player) {
        if (!player.hasPermission("advancedcoresurvival.claim.create")) {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("general.no-permission"));
            return;
        }

        Chunk chunk = player.getLocation().getChunk();
        String worldName = chunk.getWorld().getName();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();

        // Check if chunk is already claimed
        plugin.getStorageManager().getStorage().isChunkClaimed(worldName, chunkX, chunkZ).thenAccept(isClaimed -> {
            if (isClaimed) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(LocaleManager.getInstance().getFormattedMessage("claims.claim.already-claimed"));
                });
                return;
            }

            // Check if player has reached their claim limit
            plugin.getStorageManager().getStorage().getClaimCount(player.getUniqueId()).thenAccept(claimCount -> {
                int maxClaims = plugin.getConfigManager().getMaxClaimsPerPlayer();
                if (claimCount >= maxClaims) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        player.sendMessage(LocaleManager.getInstance().getFormattedMessage("claims.claim.limit-reached", "%limit%", String.valueOf(maxClaims)));
                    });
                    return;
                }

                // Proceed with claiming
                plugin.getStorageManager().getStorage().claimChunk(player.getUniqueId(), worldName, chunkX, chunkZ).thenRun(() -> {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        player.sendMessage(LocaleManager.getInstance().getFormattedMessage("claims.claim.success"));
                        // Maybe show some particle effects to indicate the chunk borders
                    });
                });
            });
        });
    }
}
