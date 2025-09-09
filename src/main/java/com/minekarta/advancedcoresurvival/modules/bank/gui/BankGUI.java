package com.minekarta.advancedcoresurvival.modules.bank.gui;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;

public class BankGUI {

    private final AdvancedCoreSurvival plugin;

    public BankGUI(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the main bank GUI for a player.
     * In the future, this will be dynamic based on the bank's status.
     *
     * @param player The player to open the GUI for.
     */
    public void open(Player player) {
        // TODO: The size and content should be dynamic based on the bank's features.
        Inventory gui = Bukkit.createInventory(null, 27, "§2Your Bank Account");

        // --- Placeholder Items ---
        // TODO: Replace with actual data-driven items.

        // Glass pane placeholders for decoration
        ItemStack placeholder = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, placeholder);
        }

        // Example: Balance Item
        // TODO: Fetch actual bank balance
        double balance = 12345.67;
        ItemStack balanceItem = createGuiItem(Material.GOLD_INGOT, "§6Your Balance",
                "§e" + String.format("%,.2f", balance) + " " + plugin.getConfig().getString("economy.currency-symbol", "$"));
        gui.setItem(11, balanceItem);


        // Example: Deposit Item
        ItemStack depositItem = createGuiItem(Material.CHEST, "§aDeposit", "§7Click to deposit funds.");
        gui.setItem(13, depositItem);

        // Example: Withdraw Item
        ItemStack withdrawItem = createGuiItem(Material.DISPENSER, "§cWithdraw", "§7Click to withdraw funds.");
        gui.setItem(15, withdrawItem);

        player.openInventory(gui);
    }

    /**
     * Helper method to create a GUI item with a name and lore.
     *
     * @param material The material of the item.
     * @param name     The name of the item.
     * @param lore     The lore of the item.
     * @return The created ItemStack.
     */
    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
