package com.minekarta.advancedcoresurvival.modules.bank.gui;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.core.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class BankGUI {

    private final AdvancedCoreSurvival plugin;
    private final Storage storage;
    private final NamespacedKey buttonKey;

    public BankGUI(AdvancedCoreSurvival plugin, Storage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.buttonKey = new NamespacedKey(plugin, "bank_gui_button");
    }

    public void open(Player player) {
        String title = "§2Player Bank";
        Inventory gui = Bukkit.createInventory(new BankGUIHolder(), 27, title);

        ItemStack placeholder = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, placeholder);
        }

        ItemStack balanceItem = createGuiItem(Material.GOLD_INGOT, "§6Bank Balance", "§e0.00");
        gui.setItem(11, balanceItem);

        ItemStack depositItem = createTaggedItem(Material.CHEST, "§aDeposit", "deposit", "§7Click to deposit funds.");
        gui.setItem(13, depositItem);

        ItemStack withdrawItem = createTaggedItem(Material.DISPENSER, "§cWithdraw", "withdraw", "§7Click to withdraw funds.");
        gui.setItem(15, withdrawItem);

        player.openInventory(gui);
    }

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

    private ItemStack createTaggedItem(Material material, String name, String tag, String... lore) {
        ItemStack item = createGuiItem(material, name, lore);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(buttonKey, PersistentDataType.STRING, tag);
            item.setItemMeta(meta);
        }
        return item;
    }

    public NamespacedKey getButtonKey() {
        return buttonKey;
    }
}
