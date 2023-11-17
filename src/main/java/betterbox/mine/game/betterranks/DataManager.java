package betterbox.mine.game.betterranks;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.Random;

public class DataManager {

    private final JavaPlugin plugin;
    private FileConfiguration dataConfig;
    private File configFile;
    private final PluginLogger pluginLogger;
    private FileConfiguration codesConfig;
    private File codesFile;
    private final Random random = new Random();

    public DataManager(JavaPlugin plugin,PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        setup();
    }

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            pluginLogger.warn("DataManager: setup: Folder does not exist");
            plugin.getDataFolder().mkdir();
            pluginLogger.debug("DataManager: setup: Folder created");
        }


        configFile = new File(plugin.getDataFolder(), "database.yml");
        if (!configFile.exists()) {
            pluginLogger.warn("DataManager: setup: Database file does not exist");
            try {
                configFile.createNewFile();
                pluginLogger.debug("DataManager: setup: Database file created");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create database.yml file!");
                e.printStackTrace();
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(configFile);
        pluginLogger.debug("DataManager: setup: Database file loaded");
        // Setting up the second database for codes
        codesFile = new File(plugin.getDataFolder(), "codes.yml");
        if (!codesFile.exists()) {
            try {
                codesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create codes.yml file!");
                e.printStackTrace();
            }
        }

        codesConfig = YamlConfiguration.loadConfiguration(codesFile);
    }

    // Generate and store unique codes
    public void generateCodes(int numberOfCodes, String rank, int timeAmount, char timeUnit) {
        for (int i = 0; i < numberOfCodes; i++) {
            String code;
            do {
                code = generateRandomCode(8); // Generate a random 8 character code
            } while (codesConfig.contains(code));

            codesConfig.set(code + ".rank", rank);
            codesConfig.set(code + ".timeAmount", timeAmount);
            codesConfig.set(code + ".timeUnit", String.valueOf(timeUnit));
        }
        saveCodes();
    }

    private String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        return code.toString();
    }
    public FileConfiguration getCodesConfig() {
        return codesConfig;
    }

    public boolean useCode(String code) {
        if (codesConfig.contains(code)) {
            codesConfig.set(code, null); // Remove code after use
            saveCodes();
            return true;
        }
        return false;
    }

    public void saveCodes() {
        try {
            codesConfig.save(codesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save codes to codes.yml!");
            e.printStackTrace();
        }
    }


    public FileConfiguration getData() {
        return dataConfig;
    }

    public void saveData() {
        try {
            dataConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data to database.yml!");
            pluginLogger.debug("DataManager: saveData: Player " +e);
        }
        pluginLogger.debug("DataManager: saveData: Data saved");
    }

    public void reloadData() {
        dataConfig = YamlConfiguration.loadConfiguration(configFile);
    }

    // Get the expiry time for the given UUID. Returns -1 if not set.
    public long getExpiryTime(UUID uuid) {
        if (dataConfig.contains(uuid.toString())) {
            return dataConfig.getLong(uuid.toString());
        }
        return -1;
    }

    // Set the expiry time for the given UUID.
    public void setExpiryTime(UUID uuid, long time) {
        dataConfig.set(uuid.toString(), time);
    }

    // Remove the data for the given UUID.
    public void removePlayerData(UUID uuid) {
        dataConfig.set(uuid.toString(), null);
    }

    // Get all UUIDs stored in the database.
    public Set<String> getAllPlayerUUIDs() {
        return dataConfig.getKeys(false);
    }
}
