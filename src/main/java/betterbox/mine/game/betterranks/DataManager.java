package betterbox.mine.game.betterranks;

import org.bukkit.configuration.InvalidConfigurationException;
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
    private File dataFile;
    private final PluginLogger pluginLogger;
    private FileConfiguration codesConfig;
    private File codesFile;
    private final Random random = new Random();

    public DataManager(JavaPlugin plugin,PluginLogger pluginLogger) {
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        setup();
    }
    // Metoda, która zwraca pozostały czas dla danego UUID w formacie "xx d xx m xx s"
    public String getRemainingTimeFormatted(UUID uuid) {
        pluginLogger.debug("DataManager: getRemainingTimeFormatted: called UUID "+uuid);
        long expiryTime = getExpiryTime(uuid);
        pluginLogger.debug("DataManager: getRemainingTimeFormatted: expiryTime " +expiryTime);
        if (expiryTime == -1) {
            return "No expiry time set"; // Lub inną wiadomość wskazującą, że czas wygaśnięcia nie jest ustawiony

        }

        long currentTime = System.currentTimeMillis();
        if (currentTime >= expiryTime) {
            return "Expired"; // Lub inną wiadomość, jeśli czas wygaśnięcia już minął
        }

        long remainingTime = expiryTime - currentTime;
        long seconds = remainingTime / 1000 % 60;
        long minutes = remainingTime / (60 * 1000) % 60;
        long hours = remainingTime / (60 * 60 * 1000) % 24;
        long days = remainingTime / (24 * 60 * 60 * 1000);

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append(" d ");
        }
        if (hours > 0) {
            sb.append(hours).append(" h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" m ");
        }
        if (seconds > 0) {
            sb.append(seconds).append(" s");
        }

        return "Rank expires in "+sb.toString().trim();
    }

    public void setup() {
        if (!plugin.getDataFolder().exists()) {
            pluginLogger.warn("DataManager: setup: Folder does not exist");
            plugin.getDataFolder().mkdir();
            pluginLogger.debug("DataManager: setup: Folder created");
        }


        dataFile = new File(plugin.getDataFolder(), "database.yml");
        if (!dataFile.exists()) {
            pluginLogger.warn("DataManager: setup: Database file does not exist");
            try {
                dataFile.createNewFile();
                pluginLogger.debug("DataManager: setup: Database file created");
            } catch (IOException e) {
                plugin.getLogger().severe("DataManager: setup:Could not create database.yml file!"+e.getMessage());

            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        pluginLogger.debug("DataManager: setup: Database file loaded");
        // Setting up the second database for codes
        codesFile = new File(plugin.getDataFolder(), "codes.yml");
        if (!codesFile.exists()) {
            try {
                codesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("DataManager: setup:Could not create codes.yml file!");
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
    public boolean checkCode(String code) {
        return codesConfig.contains(code);
    }
    public boolean useCode(String code) {
        pluginLogger.debug("DataManager: useCode called");

            pluginLogger.debug("DataManager: useCode: code "+ code+" found in the database.");
            codesConfig.set(code, null); // Remove code after use
            pluginLogger.debug("DataManager: useCode: code "+ code+" has just been used and removed from the database.");
            saveCodes();
            pluginLogger.debug("DataManager: saveCodes: contains code? "+codesConfig.contains(code));

            pluginLogger.debug("DataManager: useCode: return true");
            return true;

    }

    public void saveCodes() {
        pluginLogger.debug("DataManager: saveCodes called");
        try {
            pluginLogger.debug("DataManager: saveCodes: saving");
            codesConfig.save(codesFile);
            pluginLogger.debug("DataManager: saveCodes: saved, reloading");
            pluginLogger.debug("DataManager: saveCodes: "+codesConfig);
            reloadCodeData();
            pluginLogger.debug("DataManager: saveCodes: "+codesConfig);

        } catch (IOException e) {
            plugin.getLogger().severe("DataManager: saveCodes: "+e.getMessage()+ " "+e);
        }
    }
    public void reloadCodeData() {
        pluginLogger.debug("DataManager: reloadCodeData called");
        codesFile = new File(plugin.getDataFolder(), "codes.yml");
        codesConfig = YamlConfiguration.loadConfiguration(codesFile);
        pluginLogger.debug("DataManager: reloadCodeData: "+codesConfig);
        try {
            codesConfig.load(codesFile);
        } catch (IOException e) {
            pluginLogger.debug("DataManager: reloadCodeData: "+e.getMessage());
            throw new RuntimeException(e);
        } catch (InvalidConfigurationException e) {
            pluginLogger.debug("DataManager: reloadCodeData: "+e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public FileConfiguration getData() {
        return dataConfig;
    }

    public void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data to database.yml!");
            pluginLogger.debug("DataManager: saveData: Player " +e);
        }
        pluginLogger.debug("DataManager: saveData: Data saved");
    }

    public void reloadData() {

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
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
        pluginLogger.debug("DataManager: setExpiryTime: called, setting "+time+" for "+uuid);
        dataConfig.set(uuid.toString(), time);
        pluginLogger.debug("DataManager: setExpiryTime: calling DataManager.saveData()");
        saveData();
    }

    // Remove the data for the given UUID.
    public void removePlayerData(UUID uuid) {
        pluginLogger.debug("DataManager: removePlayerData: called");
        dataConfig.set(uuid.toString(), null);
        pluginLogger.debug("DataManager: removePlayerData: calling DataManager.saveData()");
        saveData();
    }

    // Get all UUIDs stored in the database.
    public Set<String> getAllPlayerUUIDs() {
        return dataConfig.getKeys(false);
    }
}
