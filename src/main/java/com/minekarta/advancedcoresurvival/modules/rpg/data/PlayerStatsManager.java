package com.minekarta.advancedcoresurvival.modules.rpg.data;

import org.bukkit.Bukkit;
import com.minekarta.advancedcoresurvival.core.storage.Storage;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the loading, saving, and accessing of PlayerStats objects.
 * For now, this is an in-memory cache. In the future, it will connect to a database.
 */
public class PlayerStatsManager {

    private final Map<UUID, PlayerStats> statsCache = new ConcurrentHashMap<>();
    private final double healthPerEndurance;
    private final Storage storage;

    public PlayerStatsManager(FileConfiguration config, Storage storage) {
        this.healthPerEndurance = config.getDouble("rpg.stats.health-per-endurance", 1.0);
        this.storage = storage;
    }

    /**
     * Retrieves the stats for a given player from the cache.
     * If the player is not in the cache, their data is loaded.
     *
     * @param player The player whose stats are to be retrieved.
     * @return The PlayerStats object for the player.
     */
    public PlayerStats getPlayerStats(Player player) {
        return statsCache.computeIfAbsent(player.getUniqueId(), uuid -> loadPlayerStats(player.getUniqueId()));
    }

    /**
     * Retrieves the stats for a given player UUID from the cache.
     * This is useful for offline player operations if they are cached.
     * Returns null if the player is not in the cache.
     *
     * @param uuid The UUID of the player.
     * @return The PlayerStats object or null if not cached.
     */
    public PlayerStats getPlayerStats(UUID uuid) {
        return statsCache.get(uuid);
    }

    /**
     * Loads a player's stats into the cache.
     * For now, this simply creates a new default PlayerStats object.
     * In the future, this method will load data from a database.
     *
     * @param uuid The UUID of the player to load.
     * @return The newly created PlayerStats object.
     */
    public PlayerStats loadPlayerStats(UUID uuid) {
        // This is a blocking call for simplicity. In a high-performance scenario,
        // this should be handled asynchronously with callbacks or futures.
        PlayerStats stats = storage.loadPlayerStats(uuid).join();
        statsCache.put(uuid, stats);

        // Apply health bonus if the player is online
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            applyAllBonuses(player);
        }

        return stats;
    }

    /**
     * Saves a player's stats from the cache to the database.
     *
     * @param uuid The UUID of the player to save.
     */
    public void savePlayerStats(UUID uuid) {
        PlayerStats stats = statsCache.get(uuid);
        if (stats != null) {
            storage.savePlayerStats(stats);
        }
    }

    /**
     * Saves and removes a player's stats from the cache.
     * This should be called when a player quits the server.
     *
     * @param uuid The UUID of the player to unload.
     */
    public void unloadPlayer(UUID uuid) {
        savePlayerStats(uuid);
        statsCache.remove(uuid);
    }

    /**
     * Applies all permanent stat bonuses (health, armor, etc.) to a player.
     * This should be called after stats are loaded or recalculated.
     * @param player The player to update.
     */
    public void applyAllBonuses(Player player) {
        PlayerStats stats = getPlayerStats(player);
        if (stats == null) return;

        // Apply health bonus from Endurance
        double bonusHealth = stats.getEndurance() * healthPerEndurance;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0 + bonusHealth);

        // Apply armor bonus from skills
        double armorBonus = stats.getStatBonus("ARMOR_BONUS");
        player.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armorBonus);
    }
}
