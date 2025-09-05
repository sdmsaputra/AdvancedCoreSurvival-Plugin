package com.minekarta.advancedcoresurvival.integrations.placeholderapi;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ACSExpansion extends PlaceholderExpansion {

    private final AdvancedCoreSurvival plugin;

    public ACSExpansion(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "advancedcoresurvival";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Minekarta Studio";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required on PAPI reload
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        String[] parts = params.split("_", 2);
        String module = parts[0];
        String placeholder = parts.length > 1 ? parts[1] : "";

        if (!plugin.getModuleManager().isModuleEnabled(module)) {
            return "Module Disabled";
        }

        // We will add cases for each module here
        switch (module) {
            case "economy":
                return handleEconomyPlaceholders(player, placeholder);
            case "essentials":
                return handleEssentialsPlaceholders(player, placeholder);
            // ... other modules
        }

        return null; // Let PAPI know we couldn't find a value
    }

    private String handleEconomyPlaceholders(OfflinePlayer player, String placeholder) {
        if (placeholder.equals("balance")) {
            // This is an async operation, but PAPI is sync. This is a common issue.
            // For now, we'll block and wait for the result. This is not ideal for performance.
            // A better solution would involve caching.
            try {
                double balance = plugin.getStorageManager().getStorage().getPlayerBalance(player.getUniqueId()).get();
                return String.format("%.2f", balance);
            } catch (Exception e) {
                return "Error";
            }
        }
        return null;
    }

    private String handleEssentialsPlaceholders(OfflinePlayer player, String placeholder) {
        if (placeholder.equals("home_count")) {
            try {
                int count = plugin.getStorageManager().getStorage().getHomeCount(player.getUniqueId()).get();
                return String.valueOf(count);
            } catch (Exception e) {
                return "Error";
            }
        }
        return null;
    }
}
