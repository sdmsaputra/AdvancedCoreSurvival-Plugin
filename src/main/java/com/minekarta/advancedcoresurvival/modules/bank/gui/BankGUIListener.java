package com.minekarta.advancedcoresurvival.modules.bank.gui;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.core.storage.Storage;
import com.minekarta.advancedcoresurvival.modules.bank.BankModule;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;

public class BankGUIListener implements Listener {

    private final AdvancedCoreSurvival plugin;
    private final Storage storage;
    private final BankModule bankModule;
    private final Economy economy;

    public BankGUIListener(AdvancedCoreSurvival plugin, Storage storage, BankModule bankModule) {
        this.plugin = plugin;
        this.storage = storage;
        this.bankModule = bankModule;
        this.economy = plugin.getEconomy();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BankGUIHolder)) {
            return;
        }
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getItemMeta() == null) {
            return;
        }

        String buttonType = clickedItem.getItemMeta().getPersistentDataContainer().get(bankModule.getBankGUI().getButtonKey(), PersistentDataType.STRING);
        if (buttonType == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (buttonType.equals("deposit") || buttonType.equals("withdraw")) {
            openAnvilGUI(player, buttonType);
        }
    }

    private void openAnvilGUI(Player player, String action) {
        new AnvilGUI.Builder()
            .onClick((slot, stateSnapshot) -> {
                if (slot != AnvilGUI.Slot.OUTPUT) {
                    return Collections.emptyList();
                }

                String text = stateSnapshot.getText();
                double amount;
                try {
                    amount = Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("Invalid Number"));
                }

                if (amount <= 0) {
                    return Collections.singletonList(AnvilGUI.ResponseAction.replaceInputText("Invalid Amount"));
                }

                // This is a placeholder for getting the player's bank.
                String bankName = "My Bank";

                if (action.equals("deposit")) {
                    handleDeposit(player, bankName, amount);
                } else if (action.equals("withdraw")) {
                    handleWithdraw(player, bankName, amount);
                }

                return Collections.singletonList(AnvilGUI.ResponseAction.close());
            })
            .text("0.0")
            .itemLeft(new ItemStack(Material.PAPER))
            .title(action.substring(0, 1).toUpperCase() + action.substring(1) + " Amount")
            .plugin(plugin)
            .open(player);
    }

    private void handleDeposit(Player player, String bankName, double amount) {
        if (economy.has(player, amount)) {
            EconomyResponse r = economy.withdrawPlayer(player, amount);
            if (r.transactionSuccess()) {
                storage.getBankBalance(bankName).thenAccept(balance -> {
                    storage.setBankBalance(bankName, balance + amount).thenRun(() -> {
                        player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.deposit-success", "%amount%", String.format("%,.2f", amount)));
                        Bukkit.getScheduler().runTask(plugin, () -> bankModule.getBankGUI().open(player));
                    });
                });
            } else {
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.withdraw-error-personal", "%message%", r.errorMessage));
            }
        } else {
            player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.insufficient-funds-personal"));
        }
    }

    private void handleWithdraw(Player player, String bankName, double amount) {
        storage.getBankBalance(bankName).thenAccept(balance -> {
            if (balance >= amount) {
                storage.setBankBalance(bankName, balance - amount).thenRun(() -> {
                    EconomyResponse r = economy.depositPlayer(player, amount);
                    if (r.transactionSuccess()) {
                        player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.withdraw-success", "%amount%", String.format("%,.2f", amount)));
                        Bukkit.getScheduler().runTask(plugin, () -> bankModule.getBankGUI().open(player));
                    } else {
                        // Rollback
                        storage.setBankBalance(bankName, balance);
                        player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.withdraw-error"));
                    }
                });
            } else {
                player.sendMessage(LocaleManager.getInstance().getFormattedMessage("bank.insufficient-funds-bank"));
            }
        });
    }
}
