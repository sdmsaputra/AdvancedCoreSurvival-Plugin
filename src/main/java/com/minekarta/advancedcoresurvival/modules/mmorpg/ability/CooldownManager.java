package com.minekarta.advancedcoresurvival.modules.mmorpg.ability;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    // Player UUID -> (Ability ID -> Cooldown Expiry Timestamp)
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    /**
     * Puts an ability on cooldown for a player.
     * @param playerUUID The player's UUID.
     * @param abilityId The ID of the ability.
     * @param seconds The duration of the cooldown in seconds.
     */
    public void setCooldown(UUID playerUUID, String abilityId, int seconds) {
        if (seconds <= 0) return;
        long expiryTime = System.currentTimeMillis() + (seconds * 1000L);
        cooldowns.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>()).put(abilityId.toLowerCase(), expiryTime);
    }

    /**
     * Checks if an ability is currently on cooldown for a player.
     * @param playerUUID The player's UUID.
     * @param abilityId The ID of the ability.
     * @return True if the ability is on cooldown, false otherwise.
     */
    public boolean isOnCooldown(UUID playerUUID, String abilityId) {
        return getRemainingCooldown(playerUUID, abilityId) > 0;
    }

    /**
     * Gets the remaining cooldown time for an ability.
     * @param playerUUID The player's UUID.
     * @param abilityId The ID of the ability.
     * @return The remaining time in seconds, or 0 if not on cooldown.
     */
    public int getRemainingCooldown(UUID playerUUID, String abilityId) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerUUID);
        if (playerCooldowns == null) {
            return 0;
        }

        Long expiryTime = playerCooldowns.get(abilityId.toLowerCase());
        if (expiryTime == null) {
            return 0;
        }

        long remainingMillis = expiryTime - System.currentTimeMillis();
        return remainingMillis > 0 ? (int) (remainingMillis / 1000) + 1 : 0;
    }
}
