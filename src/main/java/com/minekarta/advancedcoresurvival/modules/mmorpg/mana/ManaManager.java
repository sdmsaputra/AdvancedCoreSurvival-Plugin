package com.minekarta.advancedcoresurvival.modules.mmorpg.mana;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStatsManager;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ManaManager implements Listener {

    private final AdvancedCoreSurvival plugin;
    private final PlayerStatsManager statsManager;
    private final Map<UUID, BossBar> manaBars = new ConcurrentHashMap<>();
    private BukkitTask regenTask;

    public ManaManager(AdvancedCoreSurvival plugin, PlayerStatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    public void start() {
        double regenAmount = plugin.getConfig().getDouble("mmorpg.mana.regen-amount", 5.0);
        long interval = plugin.getConfig().getLong("mmorpg.mana.regen-interval-seconds", 2) * 20L;

        this.regenTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, interval, interval);
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Add boss bars for any players already online
        for (Player player : Bukkit.getOnlinePlayers()) {
            addPlayer(player);
        }
    }

    public void stop() {
        if (regenTask != null) {
            regenTask.cancel();
        }
        manaBars.values().forEach(BossBar::removeAll);
        manaBars.clear();
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerStats stats = statsManager.getPlayerStats(player);
            double currentMana = stats.getMana();
            double maxMana = stats.getMaxMana();
            double regenAmount = plugin.getConfig().getDouble("mmorpg.mana.regen-amount", 5.0);

            if (currentMana < maxMana) {
                stats.setMana(currentMana + regenAmount);
            }
            updateManaBar(player, stats);
        }
    }

    private void updateManaBar(Player player, PlayerStats stats) {
        BossBar bar = manaBars.get(player.getUniqueId());
        if (bar != null) {
            bar.setProgress(stats.getMana() / stats.getMaxMana());
            bar.setTitle("§bMana: §f" + (int)stats.getMana() + " / " + (int)stats.getMaxMana());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        addPlayer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    private void addPlayer(Player player) {
        BossBar bar = Bukkit.createBossBar("Mana", BarColor.BLUE, BarStyle.SOLID);
        bar.addPlayer(player);
        manaBars.put(player.getUniqueId(), bar);
        updateManaBar(player, statsManager.getPlayerStats(player));
    }

    private void removePlayer(Player player) {
        BossBar bar = manaBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }
}
