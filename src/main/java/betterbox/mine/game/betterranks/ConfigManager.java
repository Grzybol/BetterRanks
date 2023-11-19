package betterbox.mine.game.betterranks;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ConfigManager {
    private JavaPlugin plugin;
    private PluginLogger pluginLogger;

    public ConfigManager(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        initializeConfigFile();
        configureLogger();
    }

    private void initializeConfigFile() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            pluginLogger.info("Config file does not exist, creating a new one.");
            plugin.saveDefaultConfig(); // This will create the config.yml with default values
            pluginLogger.info("Default config file created successfully.");
        } else {
            pluginLogger.info("Config file already exists.");
        }
    }

    private void configureLogger() {
        // Reloads the config in case it was just created
        plugin.reloadConfig();

        // Read log_level settings from the config file
        List<String> logLevels = plugin.getConfig().getStringList("log_level");
        Set<PluginLogger.LogLevel> enabledLogLevels = EnumSet.noneOf(PluginLogger.LogLevel.class);

        for (String level : logLevels) {
            try {
                enabledLogLevels.add(PluginLogger.LogLevel.valueOf(level.toUpperCase()));
            } catch (IllegalArgumentException e) {
                pluginLogger.warn("Invalid log level in config: " + level);
            }
        }

        if (enabledLogLevels.isEmpty()) {
            enabledLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.SEVERE);
        }

        // Set active logging levels in the logger
        pluginLogger.setEnabledLogLevels(enabledLogLevels);
    }

    public void updateConfig(String configuration) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            pluginLogger.warn("Config file does not exist, creating new one.");
            plugin.saveDefaultConfig();
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(configFile.toURI()));
            lines.add("###################################");
            lines.add(configuration);

            Files.write(Paths.get(configFile.toURI()), lines);
            pluginLogger.info("Config file updated successfully.");
        } catch (IOException e) {
            pluginLogger.severe("Error while updating config file: " + e.getMessage());
        }
    }
}
