package com.minekarta.advancedcoresurvival.core.storage;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import com.minekarta.advancedcoresurvival.modules.rpg.data.PlayerStats;
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
    CompletableFuture<Double> getPlayerBalance(UUID playerUUID, String worldName);
    CompletableFuture<Void> setPlayerBalance(UUID playerUUID, String worldName, double balance);

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
    CompletableFuture<java.util.Map<UUID, Integer>> getAllPlayersAndClaimCounts();
    CompletableFuture<Boolean> isChunkClaimed(String world, int chunkX, int chunkZ);
    CompletableFuture<UUID> getClaimOwner(String world, int chunkX, int chunkZ);
    CompletableFuture<Integer> getClaimId(String world, int chunkX, int chunkZ);
    CompletableFuture<Void> claimChunk(UUID owner, String world, int chunkX, int chunkZ);
    CompletableFuture<Void> unclaimChunk(String world, int chunkX, int chunkZ);
    CompletableFuture<Void> unclaimAllChunks(UUID ownerUUID);
    CompletableFuture<Void> addClaimMember(int claimId, UUID memberUUID);
    CompletableFuture<Void> removeClaimMember(int claimId, UUID memberUUID);
    CompletableFuture<Boolean> isMemberOfClaim(int claimId, UUID memberUUID);
    CompletableFuture<List<UUID>> getClaimMembers(int claimId);
    CompletableFuture<Integer> getClaimCount(UUID ownerUUID);

    // --- RPG ---
    CompletableFuture<Void> savePlayerStats(PlayerStats stats);
    CompletableFuture<PlayerStats> loadPlayerStats(UUID playerUUID);

    // --- Banks ---
    CompletableFuture<Boolean> hasBank(String name);
    CompletableFuture<Boolean> createBank(String name, UUID owner);
    CompletableFuture<Boolean> deleteBank(String name);
    CompletableFuture<Double> getBankBalance(String name);
    CompletableFuture<Void> setBankBalance(String name, double balance);
    CompletableFuture<Boolean> isBankOwner(String name, UUID player);
    CompletableFuture<Boolean> isBankMember(String name, UUID player);
    CompletableFuture<Void> addBankMember(String name, UUID player);
    CompletableFuture<Void> removeBankMember(String name, UUID player);
    CompletableFuture<List<String>> getBanks();

    // Bank Invites
    CompletableFuture<Void> addBankInvite(int bankId, UUID invitedUUID);
    CompletableFuture<Integer> getBankInvite(UUID invitedUUID);
    CompletableFuture<Void> removeBankInvite(UUID invitedUUID);
}
