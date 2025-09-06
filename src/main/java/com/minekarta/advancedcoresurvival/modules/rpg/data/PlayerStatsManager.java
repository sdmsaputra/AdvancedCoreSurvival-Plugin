package com.minekarta.advancedcoresurvival.modules.rpg.data;

import com.minekarta.advancedcoresurvival.core.storage.Storage;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the loading, saving, and caching of PlayerStats objects
 * by interacting with a Storage backend.
 */
public class PlayerStatsManager {

    private final Map<UUID, PlayerStats> statsCache = new ConcurrentHashMap<>();
    private final Storage storage;

    public PlayerStatsManager(Storage storage) {
        this.storage = storage;
    }

    /**
     * Retrieves the stats for a given player, loading from storage if not cached.
     * This is a synchronous call that waits for the data from the database.
     *
     * @param player The player whose stats are to be retrieved.
     * @return The PlayerStats object for the player.
     */
    public PlayerStats getPlayerStats(Player player) {
        return statsCache.computeIfAbsent(player.getUniqueId(), this::loadPlayerStats);
    }

    /**
     * Asynchronously retrieves the stats for a given player.
     * This is preferred over the synchronous version if the caller can handle a CompletableFuture.
     */
    public CompletableFuture<PlayerStats> getPlayerStatsAsync(UUID uuid) {
        if (statsCache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(statsCache.get(uuid));
        }
        return storage.loadPlayerStats(uuid).thenApply(stats -> {
            statsCache.put(uuid, stats);
            return stats;
        });
    }

    /**
     * Loads a player's stats from storage into the cache.
     *
     * @param uuid The UUID of the player to load.
     * @return The newly loaded PlayerStats object.
     */
    private PlayerStats loadPlayerStats(UUID uuid) {
        return storage.loadPlayerStats(uuid).join();
    }

    /**
     * Saves a player's stats from the cache to the storage.
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
}
