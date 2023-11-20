package betterbox.mine.game.betterranks;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ConfigManager {
    private JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    private File configFile = null;
    List<String> logLevels = null;

    public ConfigManager(JavaPlugin plugin, PluginLogger pluginLogger) {

        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager called");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: calling configureLogger");
        configureLogger();
    }

    private void configureLogger() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"ConfigManager: configureLogger called");
        if (!configFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING, "Config file does not exist, creating new one.");
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Set<PluginLogger.LogLevel> enabledLogLevels;
        if (logLevels == null || logLevels.isEmpty()) {
            // Jeśli konfiguracja nie określa poziomów logowania, użyj domyślnych ustawień
            enabledLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            updateConfig("log_level:\n  - INFO\n  - WARNING\n  - ERROR");
        } else {
            enabledLogLevels = new HashSet<>();
            for (String level : logLevels) {
                try {
                    enabledLogLevels.add(PluginLogger.LogLevel.valueOf(level.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Jeśli podano nieprawidłowy poziom logowania, zaloguj błąd
                    plugin.getServer().getLogger().warning("Invalid log level in config: " + level);
                }
            }
        }
        // Odczytanie ustawień log_level z pliku konfiguracyjnego
        logLevels = plugin.getConfig().getStringList("log_level");


        // Ustawienie aktywnych poziomów logowania w loggerze
        pluginLogger.setEnabledLogLevels(enabledLogLevels);
    }

    public void updateConfig(String configuration) {





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
