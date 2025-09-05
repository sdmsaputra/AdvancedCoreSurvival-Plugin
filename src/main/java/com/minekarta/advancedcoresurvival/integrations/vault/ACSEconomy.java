package com.minekarta.advancedcoresurvival.integrations.vault;

import com.minekarta.advancedcoresurvival.api.events.ACSBalanceChangeEvent;
import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;

public class ACSEconomy implements Economy {

    private final AdvancedCoreSurvival plugin;

    public ACSEconomy(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    private EconomyResponse notImplemented() {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not implemented");
    }

    @Override
    public boolean isEnabled() {
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public String getName() {
        return "AdvancedCoreSurvival";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return String.format("%.2f", amount);
    }

    @Override
    public String currencyNamePlural() {
        return currencyNameSingular();
    }

    @Override
    public String currencyNameSingular() {
        return plugin.getConfigManager().getCurrencySymbol();
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return true; // All players have an account by default
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        try {
            return plugin.getStorageManager().getStorage().getPlayerBalance(player.getUniqueId()).get();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount.");
        }
        try {
            double oldBalance = getBalance(player);
            if (oldBalance < amount) {
                return new EconomyResponse(0, oldBalance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
            }
            double newBalance = oldBalance - amount;
            plugin.getStorageManager().getStorage().setPlayerBalance(player.getUniqueId(), newBalance).get();
            if (player.isOnline()) {
                plugin.getServer().getPluginManager().callEvent(new ACSBalanceChangeEvent(player.getPlayer(), oldBalance, newBalance));
            }
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred.");
        }
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount.");
        }
        try {
            double oldBalance = getBalance(player);
            double newBalance = oldBalance + amount;
            plugin.getStorageManager().getStorage().setPlayerBalance(player.getUniqueId(), newBalance).get();
            if (player.isOnline()) {
                plugin.getServer().getPluginManager().callEvent(new ACSBalanceChangeEvent(player.getPlayer(), oldBalance, newBalance));
            }
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred.");
        }
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true; // Accounts are created implicitly
    }

    // --- Unsupported Methods ---
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) { return hasAccount(player); }
    @Override
    public double getBalance(OfflinePlayer player, String world) { return getBalance(player); }
    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) { return has(player, amount); }
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) { return withdrawPlayer(player, amount); }
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) { return depositPlayer(player, amount); }
    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) { return createPlayerAccount(player); }
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) { return notImplemented(); }
    @Override
    public EconomyResponse deleteBank(String name) { return notImplemented(); }
    @Override
    public EconomyResponse bankBalance(String name) { return notImplemented(); }
    @Override
    public EconomyResponse bankHas(String name, double amount) { return notImplemented(); }
    @Override
    public EconomyResponse bankWithdraw(String name, double amount) { return notImplemented(); }
    @Override
    public EconomyResponse bankDeposit(String name, double amount) { return notImplemented(); }
    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return notImplemented(); }
    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) { return notImplemented(); }
    @Override
    public List<String> getBanks() { return Collections.emptyList(); }
}
