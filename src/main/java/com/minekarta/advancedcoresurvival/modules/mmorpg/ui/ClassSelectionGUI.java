package com.minekarta.advancedcoresurvival.modules.mmorpg.ui;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.modules.mmorpg.ClassManager;
import com.minekarta.advancedcoresurvival.modules.mmorpg.PlayerClass;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassSelectionGUI implements Listener {

    private final Player player;
    private final AdvancedCoreSurvival plugin;
    private final PlayerStatsManager statsManager;
    private final ClassManager classManager;
    private Inventory inventory;

    private static final String GUI_TITLE = "Choose Your Class";
    private static final String CONFIRM_GUI_TITLE = "Confirm Class Selection";

    public ClassSelectionGUI(Player player, AdvancedCoreSurvival plugin, PlayerStatsManager statsManager, ClassManager classManager) {
        this.player = player;
        this.plugin = plugin;
        this.statsManager = statsManager;
        this.classManager = classManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        int inventorySize = 27;
        this.inventory = Bukkit.createInventory(null, inventorySize, Component.text(GUI_TITLE));

        int slot = 0;
        for (PlayerClass pc : classManager.getClasses().values()) {
            inventory.setItem(slot++, createClassItem(pc));
        }

        player.openInventory(inventory);
    }

    private ItemStack createClassItem(PlayerClass pc) {
        ItemStack item = new ItemStack(pc.getIcon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(ChatColor.translateAlternateColorCodes('&', pc.getName())));

        List<String> lore = new ArrayList<>();
        pc.getDescription().forEach(line -> lore.add(ChatColor.translateAlternateColorCodes('&', line)));
        lore.add("");
        lore.add(ChatColor.GRAY + "Base Stats:");
        pc.getBaseStats().forEach((stat, value) -> {
            lore.add(ChatColor.GREEN + " + " + value + " " + capitalize(stat));
        });
        lore.add("");
        lore.add(ChatColor.YELLOW + "Click to select this class!");

        meta.lore(lore.stream().map(Component::text).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    private void openConfirmationGUI(PlayerClass selectedClass) {
        this.inventory = Bukkit.createInventory(null, 27, Component.text(CONFIRM_GUI_TITLE));

        // Confirmation item
        ItemStack confirm = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirm.getItemMeta();
        confirmMeta.displayName(Component.text("Confirm: Become a " + selectedClass.getName()).color(NamedTextColor.GREEN));
        confirm.setItemMeta(confirmMeta);

        // Cancel item
        ItemStack cancel = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancel.getItemMeta();
        cancelMeta.displayName(Component.text("Cancel").color(NamedTextColor.RED));
        cancel.setItemMeta(cancelMeta);

        inventory.setItem(11, confirm);
        inventory.setItem(15, cancel);

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != inventory) return;
        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String title = ((Component) event.getView().title()).toString();

        if (title.contains(GUI_TITLE)) {
            handleClassSelectionClick(clickedItem);
        } else if (title.contains(CONFIRM_GUI_TITLE)) {
            handleConfirmationClick(clickedItem);
        }
    }

    private void handleClassSelectionClick(ItemStack item) {
        for (PlayerClass pc : classManager.getClasses().values()) {
            if (pc.getIcon() == item.getType()) {
                openConfirmationGUI(pc);
                return;
            }
        }
    }

    private void handleConfirmationClick(ItemStack item) {
        if (item.getType() == Material.GREEN_STAINED_GLASS_PANE) {
            String className = ((Component)item.getItemMeta().displayName()).toString();
            // This is fragile. A better way would be to store the class ID in NBT.
            String classId = className.toLowerCase().split(" become a ")[1].replace("§c", "").replace("§b", "");

            PlayerClass chosenClass = classManager.getClass(classId);
            if (chosenClass != null) {
                assignClass(chosenClass);
            }
            player.closeInventory();
        } else if (item.getType() == Material.RED_STAINED_GLASS_PANE) {
            player.closeInventory();
        }
    }

    private void assignClass(PlayerClass chosenClass) {
        PlayerStats stats = statsManager.getPlayerStats(player);
        stats.setPlayerClass(chosenClass.getId());

        // Apply base stats
        chosenClass.getBaseStats().forEach((stat, value) -> {
            switch(stat.toLowerCase()) {
                case "strength": stats.setStrength(stats.getStrength() + value); break;
                case "agility": stats.setAgility(stats.getAgility() + value); break;
                case "endurance": stats.setEndurance(stats.getEndurance() + value); break;
            }
        });

        // Set mana
        stats.setMaxMana(chosenClass.getBaseMana());
        stats.setMana(chosenClass.getBaseMana());

        statsManager.savePlayerStats(player.getUniqueId());

        // A better way would be to call the listener's applyAllBonuses method
        player.sendMessage(Component.text("You have become a " + chosenClass.getName() + "!").color(NamedTextColor.GOLD));
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
