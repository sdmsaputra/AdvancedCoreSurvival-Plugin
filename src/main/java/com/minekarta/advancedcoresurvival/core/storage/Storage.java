package com.minekarta.advancedcoresurvival.core.storage;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for all data storage implementations (e.g., SQLite, MySQL).
 * This ensures that modules can interact with the database in a consistent way,
 * regardless of the underlying storage type.
 *
 * All methods that perform I/O should return a CompletableFuture to be run asynchronously.
 */
public interface Storage {

    void connect(AdvancedCoreSurvival plugin);
    void disconnect();
    boolean isConnected();

    // --- Economy ---
    CompletableFuture<Double> getPlayerBalance(UUID playerUUID);
    CompletableFuture<Void> setPlayerBalance(UUID playerUUID, double balance);

    // --- Essentials (Spawn) ---
    CompletableFuture<Void> setSpawnLocation(Location location);
    CompletableFuture<Location> getSpawnLocation();

    // --- Essentials (Homes) ---
    CompletableFuture<Void> setHome(UUID playerUUID, String name, Location location);
    CompletableFuture<Void> deleteHome(UUID playerUUID, String name);
    CompletableFuture<Location> getHome(UUID playerUUID, String name);
    CompletableFuture<Integer> getHomeCount(UUID playerUUID);
    CompletableFuture<List<String>> listHomes(UUID playerUUID);

    // --- Claims ---
    CompletableFuture<Boolean> isChunkClaimed(String world, int chunkX, int chunkZ);
    CompletableFuture<UUID> getClaimOwner(String world, int chunkX, int chunkZ);
    CompletableFuture<Integer> getClaimId(String world, int chunkX, int chunkZ);
    CompletableFuture<Void> claimChunk(UUID owner, String world, int chunkX, int chunkZ);
    CompletableFuture<Void> unclaimChunk(String world, int chunkX, int chunkZ);
    CompletableFuture<Void> addClaimMember(int claimId, UUID memberUUID);
    CompletableFuture<Void> removeClaimMember(int claimId, UUID memberUUID);
    CompletableFuture<Boolean> isMemberOfClaim(int claimId, UUID memberUUID);
    CompletableFuture<List<UUID>> getClaimMembers(int claimId);
    CompletableFuture<Integer> getClaimCount(UUID ownerUUID);
}
