package com.minekarta.advancedcoresurvival.modules.claims;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.UUID;

public class ClaimProtectionListener implements Listener {

    private final AdvancedCoreSurvival plugin;

    public ClaimProtectionListener(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

        plugin.getStorageManager().getStorage().isChunkClaimed(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()).thenAccept(isClaimed -> {
            if (!isClaimed) return;

            plugin.getStorageManager().getStorage().getClaimOwner(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()).thenAccept(ownerUUID -> {
                if (ownerUUID == null) return; // Should not happen if claimed

                if (!player.getUniqueId().equals(ownerUUID)) {
                    // Check for trusted members
                    plugin.getStorageManager().getStorage().getClaimId(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()).thenAccept(claimId -> {
                        plugin.getStorageManager().getStorage().isMemberOfClaim(claimId, player.getUniqueId()).thenAccept(isMember -> {
                            if (!isMember) {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    event.setCancelled(true);
                                    player.sendMessage(ChatColor.RED + "You can't break blocks here. This land is claimed.");
                                });
                            }
                        });
                    });
                }
            });
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Chunk chunk = event.getBlock().getChunk();

         plugin.getStorageManager().getStorage().isChunkClaimed(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()).thenAccept(isClaimed -> {
            if (!isClaimed) return;

            plugin.getStorageManager().getStorage().getClaimOwner(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()).thenAccept(ownerUUID -> {
                if (ownerUUID == null) return; // Should not happen if claimed

                if (!player.getUniqueId().equals(ownerUUID)) {
                    // Check for trusted members
                    plugin.getStorageManager().getStorage().getClaimId(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()).thenAccept(claimId -> {
                        plugin.getStorageManager().getStorage().isMemberOfClaim(claimId, player.getUniqueId()).thenAccept(isMember -> {
                            if (!isMember) {
                                plugin.getServer().getScheduler().runTask(plugin, () -> {
                                    event.setCancelled(true);
                                    player.sendMessage(ChatColor.RED + "You can't place blocks here. This land is claimed.");
                                });
                            }
                        });
                    });
                }
            });
        });
    }
}
