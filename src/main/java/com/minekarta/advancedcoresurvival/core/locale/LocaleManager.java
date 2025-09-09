package com.minekarta.advancedcoresurvival.core.locale;

import com.minekarta.advancedcoresurvival.core.AdvancedCoreSurvival;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LocaleManager {

    private static final LocaleManager instance = new LocaleManager();
    private FileConfiguration messagesConfig;
    private String prefix = "";

    private LocaleManager() {
    }

    public static LocaleManager getInstance() {
        return instance;
    }

    public void loadMessages(AdvancedCoreSurvival plugin) {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Also load from defaults in the JAR to ensure all keys are present
        InputStream defaultConfigStream = plugin.getResource("messages.yml");
        if (defaultConfigStream != null) {
            messagesConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream)));
        }
        this.prefix = messagesConfig.getString("prefix", "&8[&aACS&8] &r");
    }

    public String getMessage(String key) {
        return messagesConfig.getString(key, "Missing message: " + key);
    }

    public String getFormattedMessage(String key, String... replacements) {
        String message = getMessage(key);

        if (replacements != null) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    message = message.replace(replacements[i], replacements[i + 1]);
                }
            }
        }

        // Return empty string if message is "none" or empty, so we can disable messages
        if (message.equalsIgnoreCase("none") || message.isEmpty()) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public String getRawFormattedMessage(String key, String... replacements) {
        String message = getMessage(key);

        if (replacements != null) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    message = message.replace(replacements[i], replacements[i + 1]);
                }
            }
        }

        // Return empty string if message is "none" or empty, so we can disable messages
        if (message.equalsIgnoreCase("none") || message.isEmpty()) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
}
