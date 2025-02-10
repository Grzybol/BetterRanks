package betterbox.mine.game.betterranks;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class Lang {
    private final JavaPlugin plugin;
    private final PluginLogger pluginLogger;
    public String currentBonusString = "§6§lCurrent Bonus: ";
    public String noPermission = " You do not have permission to use this command!";
    public String higherRank = " You already have a higher rank.";
    public String poolAlreadyUsed = " You already used a code from that pool!";
    public String codeUsedSuccessfully = " Code used successfully!";
    public String invalidOrExpiredCode = " Invalid or expired code!";
    public String invalidCommand = "Invalid command usage. Check the command syntax.";
    public String timeLeftMessage = "Time left: ";
    public String timeLeftHelpMessage = " - returns time left on your current rank.";
    public String noExpiryTmeSet = "No expiry time set for this rank.";
    public String expired = "Expired";
    public String rankExpiresIn = "Rank expires in: ";


    public Lang(JavaPlugin plugin, PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        loadLangFile();
    }

    public void loadLangFile() {
        String transactionID = UUID.randomUUID().toString();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Lang.loadLangFile called", transactionID);
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang directory...", transactionID);
            langDir.mkdirs();
        }

        File langFile = new File(langDir, "lang.yml");
        if (!langFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang.yml file...", transactionID);
            createDefaultLangFile(langFile, transactionID);
        }

        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Loading lang.yml file...", transactionID);
        FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
        validateAndLoadConfig(config, langFile, transactionID);
    }

    private void createDefaultLangFile(File langFile, String transactionID) {
        try {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "Creating lang.yml file...", transactionID);
            langFile.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);
            setDefaultValues(config);
            config.save(langFile);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file created successfully!", transactionID);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error creating lang.yml file: " + e.getMessage(), transactionID);
        }
    }

    private void setDefaultValues(FileConfiguration config) {
        config.set("currentBonusString", currentBonusString);
        config.set("noPermission", noPermission);
    }

    private void validateAndLoadConfig(FileConfiguration config, File langFile, String transactionID) {
        boolean saveRequired = false;

        if (!config.contains("currentBonusString")) {
            config.set("currentBonusString", currentBonusString);
            saveRequired = true;
        } else {
            currentBonusString = config.getString("currentBonusString");
        }

        if (!config.contains("noPermission")) {
            config.set("noPermission", noPermission);
            saveRequired = true;
        } else {
            noPermission = config.getString("noPermission");
        }


        if (saveRequired) {
            try {
                config.save(langFile);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "lang.yml file updated with missing values", transactionID);
            } catch (IOException e) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR, "Error saving lang.yml file: " + e.getMessage(), transactionID);
            }
        }
    }
}