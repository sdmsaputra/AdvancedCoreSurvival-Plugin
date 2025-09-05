package com.minekarta.advancedcoresurvival.core.storage;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;

public class StorageManager {

    private final AdvancedCoreSurvival plugin;
    private Storage storage;

    public StorageManager(AdvancedCoreSurvival plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the storage provider based on the plugin configuration.
     */
    public void initializeStorage() {
        String storageType = plugin.getConfigManager().getStorageType();
        plugin.getLogger().info("Initializing storage provider... Type: " + storageType.toUpperCase());

        // We will create the SQLiteStorage class in the next step.
        // For now, this structure allows us to easily add more types later.
        if (storageType.equalsIgnoreCase("sqlite")) {
            storage = new SQLiteStorage(plugin);
        } else if (storageType.equalsIgnoreCase("mysql")) {
            // storage = new MySQLStorage(plugin);
            plugin.getLogger().warning("MySQL storage is selected, but not yet implemented.");
        } else {
            plugin.getLogger().severe("Invalid storage type '" + storageType + "' in config.yml. The plugin will not be able to save data.");
            return;
        }

        if (storage != null) {
            try {
                storage.connect(plugin);
                plugin.getLogger().info("Storage provider connected successfully.");
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to connect to the storage provider. Data will not be saved.");
                e.printStackTrace();
                this.storage = null; // Ensure we don't use a broken storage object
            }
        }
    }

    /**
     * Shuts down the active storage provider, closing any connections.
     */
    public void shutdownStorage() {
        if (storage != null && storage.isConnected()) {
            storage.disconnect();
            plugin.getLogger().info("Storage provider disconnected.");
        }
    }

    /**
     * Gets the active storage provider instance.
     * @return The active Storage interface, or null if initialization failed.
     */
    public Storage getStorage() {
        return storage;
    }
}
