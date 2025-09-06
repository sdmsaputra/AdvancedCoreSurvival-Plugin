package com.minekarta.advancedcoresurvival.modules.rpg.ui;

import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import com.minekarta.advancedcoresurvival.modules.rpg.skills.Skill;
import com.minekarta.advancedcoresurvival.modules.rpg.skills.SkillManager;
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

public class SkillTreeGUI implements Listener {

    private final Player player;
    private final PlayerStatsManager statsManager;
    private final SkillManager skillManager;
    private Inventory inventory;

    private static final String GUI_TITLE = "Pohon Keahlian";

    public SkillTreeGUI(Player player, PlayerStatsManager statsManager, SkillManager skillManager) {
        this.player = player;
        this.statsManager = statsManager;
        this.skillManager = skillManager;
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]); // A bit hacky, should be improved
    }

    public void open() {
        PlayerStats stats = statsManager.getPlayerStats(player);
        int inventorySize = 54; // 6 rows
        this.inventory = Bukkit.createInventory(null, inventorySize, Component.text(GUI_TITLE));

        // Populate GUI with skills
        int slot = 0;
        for (Skill skill : skillManager.getSkills().values()) {
            if (slot >= inventorySize) break;
            inventory.setItem(slot++, createSkillItem(skill, stats));
        }

        player.openInventory(inventory);
    }

    private ItemStack createSkillItem(Skill skill, PlayerStats stats) {
        ItemStack item = new ItemStack(skill.getIcon());
        ItemMeta meta = item.getItemMeta();

        int currentLevel = stats.getSkillLevel(skill.getId());
        boolean canAfford = stats.getSkillPoints() >= skill.getCost();
        boolean maxLevel = currentLevel >= skill.getMaxLevel();

        meta.displayName(Component.text(ChatColor.translateAlternateColorCodes('&', skill.getName())));

        List<String> lore = new ArrayList<>();
        for (String line : skill.getDescription()) {
            lore.add(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(line, skill, stats)));
        }

        if (maxLevel) {
            lore.add(ChatColor.GOLD + "Level Maksimal Tercapai");
        } else if (canAfford) {
            lore.add(ChatColor.GREEN + "Klik untuk meningkatkan!");
        } else {
            lore.add(ChatColor.RED + "Poin keahlian tidak cukup.");
        }

        meta.lore(lore.stream().map(Component::text).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    private String replacePlaceholders(String text, Skill skill, PlayerStats stats) {
        int currentLevel = stats.getSkillLevel(skill.getId());
        double currentBonus = skill.getValue() * currentLevel;

        return text.replace("{level}", String.valueOf(currentLevel))
                   .replace("{max_level}", String.valueOf(skill.getMaxLevel()))
                   .replace("{cost}", String.valueOf(skill.getCost()))
                   .replace("{current_bonus}", String.format("%.1f", currentBonus * 100)); // For percentage bonuses
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Find which skill was clicked
        for (Skill skill : skillManager.getSkills().values()) {
            if (skill.getIcon() == clickedItem.getType()) {
                // Found the skill, process the click
                handleSkillClick(skill);
                return;
            }
        }
    }

    private void handleSkillClick(Skill skill) {
        PlayerStats stats = statsManager.getPlayerStats(player);
        int currentLevel = stats.getSkillLevel(skill.getId());

        // --- Pre-purchase Checks ---
        if (currentLevel >= skill.getMaxLevel()) {
            player.sendMessage(Component.text("You have already maxed out this skill.").color(NamedTextColor.RED));
            return;
        }

        if (stats.getSkillPoints() < skill.getCost()) {
            player.sendMessage(Component.text("You do not have enough skill points.").color(NamedTextColor.RED));
            return;
        }

        for (String requiredSkillId : skill.getRequirements()) {
            if (stats.getSkillLevel(requiredSkillId) < 1) {
                Skill requiredSkill = skillManager.getSkill(requiredSkillId);
                String requiredSkillName = requiredSkill != null ? requiredSkill.getName() : "an unknown skill";
                player.sendMessage(Component.text("You must unlock " + requiredSkillName + " first.").color(NamedTextColor.RED));
                return;
            }
        }

        // --- Process Purchase ---
        stats.setSkillPoints(stats.getSkillPoints() - skill.getCost());
        stats.setSkillLevel(skill.getId(), currentLevel + 1);

        // Recalculate and apply bonuses
        stats.recalculateStatBonuses(skillManager);
        statsManager.applyAllBonuses(player);

        // --- Feedback ---
        player.sendMessage(Component.text("You have upgraded " + skill.getName() + " to Level " + (currentLevel + 1) + "!").color(NamedTextColor.GREEN));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        // Re-open the GUI to show the changes
        open();
    }
}
