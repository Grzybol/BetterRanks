package betterbox.mine.game.betterranks;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ConfigManager {
    private JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private File configFile = null;
    List<String> logLevels = null;
    Set<PluginLogger.LogLevel> enabledLogLevels;
    private Map<Integer, String> rankHierarchy;

    public ConfigManager(JavaPlugin plugin, PluginLogger pluginLogger, String folderPath) {

        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        this.rankHierarchy = new LinkedHashMap<>();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager called");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: calling configureLogger");
        configureLogger();
        CreateExampleConfigFile(folderPath);
    }
    private void CreateExampleConfigFile(String folderPath){
        File exampleConfigFile = new File(folderPath, "config.yml");
        try (InputStream in = plugin.getResource("exampleFiles/config.yml")) {
            if (in == null) {
                plugin.getLogger().severe("Resource 'customDropTables/spawners.yml not found.");
                return;
            }
            Files.copy(in, exampleConfigFile.toPath());
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml to " + exampleConfigFile + ": " + e.getMessage());
        }
    }

    private void configureLogger() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: configureLogger called");
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config file does not exist, creating new one.");
            try {
                configFile.createNewFile();
                updateConfig("log_level:\n  - INFO\n  - WARNING\n  - ERROR");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        ReloadConfig();
    }
    public void ReloadConfig(){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: ReloadConfig called");
        // Odczytanie ustawień log_level z pliku konfiguracyjnego
        configFile = new File(plugin.getDataFolder(), "config.yml");
        plugin.reloadConfig();
        logLevels = plugin.getConfig().getStringList("log_level");
        enabledLogLevels = new HashSet<>();
        if (logLevels == null || logLevels.isEmpty()) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"ConfigManager: ReloadConfig: no config file or no configured log levels! Saving default settings.");
            // Jeśli konfiguracja nie określa poziomów logowania, użyj domyślnych ustawień
            enabledLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            updateConfig("log_level:\n  - INFO\n  - WARNING\n  - ERROR");

        }

        for (String level : logLevels) {
            try {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"ConfigManager: ReloadConfig: adding "+level.toUpperCase());
                enabledLogLevels.add(PluginLogger.LogLevel.valueOf(level.toUpperCase()));
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"ConfigManager: ReloadConfig: current log levels: "+ Arrays.toString(enabledLogLevels.toArray()));

            } catch (IllegalArgumentException e) {
                // Jeśli podano nieprawidłowy poziom logowania, zaloguj błąd
                plugin.getServer().getLogger().warning("Invalid log level in config: " + level);
            }
        }
        if (plugin.getConfig().contains("groups")) {
            ConfigurationSection groupSection = plugin.getConfig().getConfigurationSection("groups");
            if (groupSection != null) {
                rankHierarchy.clear();
                for (String key : groupSection.getKeys(false)) {
                    try {
                        int rankPosition = Integer.parseInt(key);
                        String rankName = groupSection.getString(key);
                        rankHierarchy.put(rankPosition, rankName);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"ConfigManager: ReloadConfig: added rank " + rankName + " at position " + rankPosition);
                    } catch (NumberFormatException e) {
                        pluginLogger.log(PluginLogger.LogLevel.ERROR, "Invalid rank position: " + key);
                    }
                }
            }
        } else {
            // Użyj metody do wygenerowania domyślnej hierarchii
            generateDefaultRankHierarchy();
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Rank hierarchy not found in config, using default.");
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: ReloadConfig: calling pluginLogger.setEnabledLogLevels(enabledLogLevels) with parameters: "+ Arrays.toString(enabledLogLevels.toArray()));

        // Ustawienie aktywnych poziomów logowania w loggerze
        pluginLogger.setEnabledLogLevels(enabledLogLevels);
    }
    public Map<Integer, String> getRankHierarchy() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager: getRankHierarchy called");
        return rankHierarchy;
    }


    private void generateDefaultRankHierarchy() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager: generateDefaultRankHierarchy called");
        Map<Integer, String> defaultHierarchy = new LinkedHashMap<>();
        defaultHierarchy.put(1, "Player");
        defaultHierarchy.put(2, "VIP");
        // ... dodaj resztę rang
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager: generateDefaultRankHierarchy: calling updateRankHierarchy(defaultHierarchy) "+defaultHierarchy);
        updateRankHierarchy(defaultHierarchy); // Aktualizacja pliku config.yml
    }
    public void updateRankHierarchy(Map<Integer, String> rankMap) {
        StringBuilder hierarchyConfig = new StringBuilder("groups:\n");
        for (Map.Entry<Integer, String> entry : rankMap.entrySet()) {

            hierarchyConfig.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "ConfigManager: updateRankHierarchy: hierarchyConfig "+hierarchyConfig);
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager: updateRankHierarchy: calling updateConfig(hierarchyConfig.toString()) "+hierarchyConfig);
        updateConfig(hierarchyConfig.toString());
        pluginLogger.log(PluginLogger.LogLevel.INFO, "Rank hierarchy updated successfully.");
    }



    public void updateConfig(String configuration) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "ConfigManager: updateConfig called with parameters "+ configuration);
        try {
            List<String> lines = Files.readAllLines(Paths.get(configFile.toURI()));

            // Dodaj nowe zmienne konfiguracyjne
            lines.add("###################################");
            lines.add(configuration);
            // Tutaj możemy dodać nowe zmienne konfiguracyjne
            // ...

            Files.write(Paths.get(configFile.toURI()), lines);
            pluginLogger.log(PluginLogger.LogLevel.INFO, "Config file updated successfully.");
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error while updating config file: " + e.getMessage());
        }
    }
}
