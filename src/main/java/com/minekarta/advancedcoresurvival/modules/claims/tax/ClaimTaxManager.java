package com.minekarta.advancedcoresurvival.modules.claims.tax;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.logging.Level;

public class ClaimTaxManager {

    private final AdvancedCoreSurvival plugin;
    private Economy economy;
    private BukkitTask taxTask;

    public ClaimTaxManager(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!setupEconomy()) {
            plugin.getLogger().severe("Could not setup Vault economy for claim tax. Tax system disabled.");
            return;
        }

        double taxCost = plugin.getConfig().getDouble("claims.internal.tax.cost-per-chunk-per-day", 0.0);
        if (taxCost <= 0) {
            plugin.getLogger().info("Claim tax is disabled as 'cost-per-chunk-per-day' is not set.");
            return;
        }

        // Schedule the task to run every 24 hours (1,728,000 ticks)
        long delay = 20L * 60 * 60 * 24;
        this.taxTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::collectTaxes, delay, delay);
        plugin.getLogger().info("Claim tax system enabled. Taxes will be collected every 24 hours.");
    }

    public void stop() {
        if (taxTask != null && !taxTask.isCancelled()) {
            taxTask.cancel();
            taxTask = null;
        }
    }

    public void collectTaxes() {
        plugin.getLogger().info("Starting daily claim tax collection...");
        double taxPerChunk = plugin.getConfig().getDouble("claims.internal.tax.cost-per-chunk-per-day");

        plugin.getStorageManager().getStorage().getAllPlayersAndClaimCounts().thenAccept(claimCounts -> {
            for (var entry : claimCounts.entrySet()) {
                UUID playerUUID = entry.getKey();
                int count = entry.getValue();
                double totalTax = count * taxPerChunk;

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);

                if (!economy.has(offlinePlayer, totalTax)) {
                    // Player cannot afford tax
                    handleTaxFailure(offlinePlayer, totalTax);
                    continue;
                }

                // Withdraw tax
                economy.withdrawPlayer(offlinePlayer, totalTax);

                if (offlinePlayer.isOnline()) {
                    offlinePlayer.getPlayer().sendMessage("§a$" + totalTax + " has been collected for your " + count + " claimed chunk(s).");
                }
            }
            plugin.getLogger().info("Daily claim tax collection finished.");
        });
    }

    private void handleTaxFailure(OfflinePlayer player, double taxDue) {
        String onFailure = plugin.getConfig().getString("claims.internal.tax.on-failure", "NONE").toUpperCase();

        if (player.isOnline()) {
            player.getPlayer().sendMessage("§cYou could not afford your daily claim tax of $" + taxDue + ".");
        }

        if (onFailure.equals("UNCLAIM_ALL")) {
            plugin.getLogger().info("Player " + player.getName() + " (" + player.getUniqueId() + ") failed to pay taxes. Unclaiming all their chunks.");
            plugin.getStorageManager().getStorage().unclaimAllChunks(player.getUniqueId()).thenRun(() -> {
                if (player.isOnline()) {
                    player.getPlayer().sendMessage("§cAs you could not afford the tax, all your claimed lands have been released.");
                }
            });
        }
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }
}
