package com.minekarta.advancedcoresurvival.modules.rpg.ui;

import com.minekarta.advancedcoresurvival.core.locale.LocaleManager;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import com.minekarta.advancedcoresurvival.modules.rpg.skills.Skill;
import com.minekarta.advancedcoresurvival.modules.rpg.skills.SkillManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    public SkillTreeGUI(Player player, PlayerStatsManager statsManager, SkillManager skillManager) {
        this.player = player;
        this.statsManager = statsManager;
        this.skillManager = skillManager;
        Bukkit.getPluginManager().registerEvents(this, Bukkit.getPluginManager().getPlugins()[0]); // A bit hacky, should be improved
    }

    public void open() {
        PlayerStats stats = statsManager.getPlayerStats(player);
        int inventorySize = 54; // 6 rows
        String title = LocaleManager.getInstance().getRawFormattedMessage("rpg.gui.title");
        this.inventory = Bukkit.createInventory(null, inventorySize, LegacyComponentSerializer.legacyAmpersand().deserialize(title));

        // Populate GUI with skills
        int slot = 0;
        for (Skill skill : skillManager.getSkills().values()) {
            if (slot >= 45) break; // Leave last row for info
            inventory.setItem(slot++, createSkillItem(skill, stats));
        }

        // Add player stats item
        ItemStack statsItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta statsMeta = statsItem.getItemMeta();
        String statsName = LocaleManager.getInstance().getRawFormattedMessage("rpg.gui.stats-item.name");
        statsMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(statsName));

        List<String> statsLoreConfig = LocaleManager.getInstance().getMessagesConfig().getStringList("rpg.gui.stats-item.lore");
        List<Component> statsLore = new ArrayList<>();
        for (String line : statsLoreConfig) {
            line = line.replace("%level%", String.valueOf(stats.getLevel()))
                       .replace("%points%", String.valueOf(stats.getSkillPoints()))
                       .replace("%exp%", String.format("%,.0f", stats.getExp()))
                       .replace("%req_exp%", "1000"); // Placeholder
            statsLore.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
        }
        statsMeta.lore(statsLore);
        statsItem.setItemMeta(statsMeta);

        inventory.setItem(49, statsItem); // Bottom middle slot

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
            lore.add(LocaleManager.getInstance().getRawFormattedMessage("rpg.gui.skill-item.max-level"));
        } else if (canAfford) {
            lore.add(LocaleManager.getInstance().getRawFormattedMessage("rpg.gui.skill-item.can-afford"));
        } else {
            lore.add(LocaleManager.getInstance().getRawFormattedMessage("rpg.gui.skill-item.cant-afford"));
        }

        meta.lore(lore.stream().map(LegacyComponentSerializer.legacyAmpersand()::deserialize).collect(Collectors.toList()));
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
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(LocaleManager.getInstance().getFormattedMessage("rpg.skill-max-level")));
            return;
        }

        if (stats.getSkillPoints() < skill.getCost()) {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(LocaleManager.getInstance().getFormattedMessage("rpg.not-enough-skill-points")));
            return;
        }

        for (String requiredSkillId : skill.getRequirements()) {
            if (stats.getSkillLevel(requiredSkillId) < 1) {
                Skill requiredSkill = skillManager.getSkill(requiredSkillId);
                String requiredSkillName = requiredSkill != null ? requiredSkill.getName() : "an unknown skill";
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(LocaleManager.getInstance().getFormattedMessage("rpg.skill-unlock-required", "%skill_name%", requiredSkillName)));
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
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(LocaleManager.getInstance().getFormattedMessage("rpg.skill-upgraded", "%skill_name%", skill.getName(), "%level%", String.valueOf(currentLevel + 1))));
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        // Re-open the GUI to show the changes
        open();
    }
}
