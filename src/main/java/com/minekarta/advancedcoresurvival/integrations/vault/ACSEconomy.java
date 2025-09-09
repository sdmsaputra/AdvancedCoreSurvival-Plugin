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
        return true;
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
        // If no world is specified, default to the player's current world, or the main world if offline.
        String worldName = (player.isOnline() && player.getPlayer() != null)
                ? player.getPlayer().getWorld().getName()
                : plugin.getServer().getWorlds().get(0).getName();
        return getBalance(player, worldName);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        String worldName = (player.isOnline() && player.getPlayer() != null)
                ? player.getPlayer().getWorld().getName()
                : plugin.getServer().getWorlds().get(0).getName();
        return withdrawPlayer(player, worldName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        String worldName = (player.isOnline() && player.getPlayer() != null)
                ? player.getPlayer().getWorld().getName()
                : plugin.getServer().getWorlds().get(0).getName();
        return depositPlayer(player, worldName, amount);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true; // Accounts are created implicitly
    }

    @Override
    @Deprecated
    public boolean hasAccount(String playerName) {
        return hasAccount(plugin.getServer().getOfflinePlayer(playerName));
    }

    @Override
    @Deprecated
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(plugin.getServer().getOfflinePlayer(playerName), worldName);
    }

    @Override
    @Deprecated
    public double getBalance(String playerName) {
        return getBalance(plugin.getServer().getOfflinePlayer(playerName));
    }

    @Override
    @Deprecated
    public double getBalance(String playerName, String world) {
        return getBalance(plugin.getServer().getOfflinePlayer(playerName), world);
    }

    @Override
    @Deprecated
    public boolean has(String playerName, double amount) {
        return has(plugin.getServer().getOfflinePlayer(playerName), amount);
    }

    @Override
    @Deprecated
    public boolean has(String playerName, String worldName, double amount) {
        return has(plugin.getServer().getOfflinePlayer(playerName), worldName, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(plugin.getServer().getOfflinePlayer(playerName), amount);
    }

    @Override
    @Deprecated
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(plugin.getServer().getOfflinePlayer(playerName), worldName, amount);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(plugin.getServer().getOfflinePlayer(playerName), amount);
    }

    @Override
    @Deprecated
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(plugin.getServer().getOfflinePlayer(playerName), worldName, amount);
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(plugin.getServer().getOfflinePlayer(playerName));
    }

    @Override
    @Deprecated
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(plugin.getServer().getOfflinePlayer(playerName), worldName);
    }

    @Override
    @Deprecated
    public EconomyResponse createBank(String name, String player) {
        return createBank(name, plugin.getServer().getOfflinePlayer(player));
    }

    @Override
    @Deprecated
    public EconomyResponse isBankOwner(String name, String playerName) {
        return isBankOwner(name, plugin.getServer().getOfflinePlayer(playerName));
    }

    @Override
    @Deprecated
    public EconomyResponse isBankMember(String name, String playerName) {
        return isBankMember(name, plugin.getServer().getOfflinePlayer(playerName));
    }

    // --- World-Specific Methods ---
    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        // In this implementation, accounts are implicit and exist everywhere.
        return true;
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        if (world == null) {
            return getBalance(player);
        }
        try {
            // .get() is used for simplicity, but a real implementation should handle the Future properly.
            return plugin.getStorageManager().getStorage().getPlayerBalance(player.getUniqueId(), world).get();
        } catch (Exception e) {
            plugin.getLogger().severe("Error getting balance for " + player.getName() + " in world " + world + ": " + e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return getBalance(player, worldName) >= amount;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount.");
        }
        if (worldName == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "World name cannot be null.");
        }
        try {
            double oldBalance = getBalance(player, worldName);
            if (oldBalance < amount) {
                return new EconomyResponse(0, oldBalance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
            }
            double newBalance = oldBalance - amount;
            plugin.getStorageManager().getStorage().setPlayerBalance(player.getUniqueId(), worldName, newBalance).get();
            if (player.isOnline()) {
                plugin.getServer().getPluginManager().callEvent(new ACSBalanceChangeEvent(player.getPlayer(), oldBalance, newBalance));
            }
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred during withdrawal.");
        }
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount.");
        }
        if (worldName == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "World name cannot be null.");
        }
        try {
            double oldBalance = getBalance(player, worldName);
            double newBalance = oldBalance + amount;
            plugin.getStorageManager().getStorage().setPlayerBalance(player.getUniqueId(), worldName, newBalance).get();
            if (player.isOnline()) {
                plugin.getServer().getPluginManager().callEvent(new ACSBalanceChangeEvent(player.getPlayer(), oldBalance, newBalance));
            }
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred during deposit.");
        }
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        // Accounts are created implicitly on first transaction.
        return true;
    }

    // --- Bank Methods ---
    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        try {
            boolean success = plugin.getStorageManager().getStorage().createBank(name, player.getUniqueId()).get();
            if (success) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "Bank created.");
            } else {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank name already exists.");
            }
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred.");
        }
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        try {
            boolean success = plugin.getStorageManager().getStorage().deleteBank(name).get();
            if (success) {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "Bank deleted.");
            } else {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Bank not found.");
            }
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred.");
        }
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        try {
            double balance = plugin.getStorageManager().getStorage().getBankBalance(name).get();
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred.");
        }
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        try {
            double balance = plugin.getStorageManager().getStorage().getBankBalance(name).get();
            if (balance >= amount) {
                return new EconomyResponse(0, balance, EconomyResponse.ResponseType.SUCCESS, null);
            } else {
                return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
            }
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred.");
        }
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        if (amount < 0) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative amount.");
        try {
            double oldBalance = plugin.getStorageManager().getStorage().getBankBalance(name).get();
            if (oldBalance < amount) {
                return new EconomyResponse(0, oldBalance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds.");
            }
            double newBalance = oldBalance - amount;
            plugin.getStorageManager().getStorage().setBankBalance(name, newBalance).get();
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred.");
        }
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        if (amount < 0) return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative amount.");
        try {
            double oldBalance = plugin.getStorageManager().getStorage().getBankBalance(name).get();
            double newBalance = oldBalance + amount;
            plugin.getStorageManager().getStorage().setBankBalance(name, newBalance).get();
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, null);
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred.");
        }
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        try {
            boolean isOwner = plugin.getStorageManager().getStorage().isBankOwner(name, player.getUniqueId()).get();
            return new EconomyResponse(0, 0, isOwner ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "");
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred.");
        }
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        try {
            boolean isMember = plugin.getStorageManager().getStorage().isBankMember(name, player.getUniqueId()).get();
            return new EconomyResponse(0, 0, isMember ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, "");
        } catch (Exception e) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "An error occurred.");
        }
    }

    @Override
    public List<String> getBanks() {
        try {
            return plugin.getStorageManager().getStorage().getBanks().get();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
