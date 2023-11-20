package betterbox.mine.game.betterranks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
    public String poolName =null;
    String usedPoolsPath=null;

    public DataManager(JavaPlugin plugin,PluginLogger pluginLogger) {
        this.pluginLogger = pluginLogger;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager called");
        this.plugin = plugin;

        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: calling setup()");
        setup();
    }
    // Metoda, która zwraca pozostały czas dla danego UUID w formacie "xx d xx m xx s"
    public String getRemainingTimeFormatted(UUID uuid) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getRemainingTimeFormatted: called UUID "+uuid);
        long expiryTime = getExpiryTime(uuid);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getRemainingTimeFormatted: expiryTime " +expiryTime);
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
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: setup called");
        if (!plugin.getDataFolder().exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING,"DataManager: setup: Folder does not exist");
            plugin.getDataFolder().mkdir();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: setup: Folder created");
        }


        dataFile = new File(plugin.getDataFolder(), "database.yml");
        if (!dataFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING,"DataManager: setup: Database file does not exist");
            try {
                dataFile.createNewFile();
                pluginLogger.log(PluginLogger.LogLevel.WARNING,"DataManager: setup: Database file created");
            } catch (IOException e) {
                plugin.getLogger().severe("DataManager: setup:Could not create database.yml file!"+e.getMessage());

            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: setup: Database file loaded");
        // Setting up the second database for codes
        codesFile = new File(plugin.getDataFolder(), "codes.yml");
        if (!codesFile.exists()) {
            try {
                codesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("DataManager: setup:Could not create codes.yml file!"+e.getMessage());
            }
        }

        codesConfig = YamlConfiguration.loadConfiguration(codesFile);
    }
    public String getPoolNameForCode(String code) {
        if (codesConfig.contains(code)) {
            return codesConfig.getString(code + ".pool");
        }
        return null; // Zwróć null, jeśli kod nie istnieje
    }

    // Generate and store unique codes
    public void generateCodes(int numberOfCodes, String rank, int timeAmount, char timeUnit, String poolName) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: generateCodes called with parameters: "+numberOfCodes+" "+rank+" "+timeAmount+" "+timeUnit+" "+poolName);
        for (int i = 0; i < numberOfCodes; i++) {
            String code;
            do {
                code = generateRandomCode(8);
            } while (codesConfig.contains(code));

            codesConfig.set(code + ".pool", poolName);
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
    public String getOnlinePlayerNameByUUID(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            return player.getName(); // Zwraca nick gracza
        } else {
            return null; // Gracz nie jest online lub nie istnieje
        }
    }
    public boolean canUseCode(UUID playerUuid, String code){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: canUseCode called");

        poolName = codesConfig.getString(code + ".pool");

        // Sprawdzamy, czy gracz już użył kodu z tej pule
        String playerPath = playerUuid.toString();
        usedPoolsPath = playerPath + ".usedPools." + poolName;
        if (dataConfig.contains(usedPoolsPath)) {
            pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+getOnlinePlayerNameByUUID(playerUuid)+" already used a code "+code+" from "+getPoolNameForCode(code)+" pool");
            return false; // Gracz już użył kodu z tej pule
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: canUseCode true");
        return true;
    }
    public boolean useCode(UUID playerUuid, String code) {
        if (codesConfig.contains(code)) {


            // Usuwamy kod po użyciu
            codesConfig.set(code, null);

            // Dodajemy informację, że gracz użył kodu z tej pule
            dataConfig.set(usedPoolsPath, true);

            // Zapisujemy zmiany
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: useCode: calling saveCodes()");
            saveCodes();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: useCode: calling saveData()");
            saveData();
            pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+getOnlinePlayerNameByUUID(playerUuid)+" used a code "+code+" from "+getPoolNameForCode(code)+" pool");
            return true;
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleAddCommand: playerUuid " + playerUuid + " used wrong code "+code);
        return false; // Kod nie istnieje
    }




    public void saveCodes() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveCodes called");
        try {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveCodes: saving");
            codesConfig.save(codesFile);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveCodes: saved, reloading");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveCodes: "+codesConfig);
            reloadCodeData();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveCodes: "+codesConfig);

        } catch (IOException e) {
            plugin.getLogger().severe("DataManager: saveCodes: "+e.getMessage()+ " "+e);
        }
    }
    public void reloadCodeData() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: reloadCodeData called");
        codesFile = new File(plugin.getDataFolder(), "codes.yml");
        codesConfig = YamlConfiguration.loadConfiguration(codesFile);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: reloadCodeData: "+codesConfig);
        try {
            codesConfig.load(codesFile);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: reloadCodeData: "+e.getMessage());
            throw new RuntimeException(e);
        } catch (InvalidConfigurationException e) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: reloadCodeData: "+e.getMessage());
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
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveData: Player " +e);
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveData: Data saved");
    }

    public void reloadData() {

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    // Get the expiry time for the given UUID. Returns -1 if not set.
    public long getExpiryTime(UUID uuid) {
        String expirationPath = uuid.toString() + ".expiration";
        if (dataConfig.contains(expirationPath)) {
            return dataConfig.getLong(expirationPath);
        }
        return -1; // Zwraca -1, jeśli czas wygaśnięcia nie jest ustawiony
    }


    // Set the expiry time for the given UUID.
    public void setExpiryTime(UUID uuid, long time) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: setExpiryTime: called, setting "+time+" for "+uuid);

        String playerPath = uuid.toString();
        String expirationPath = playerPath + ".expiration";

        // Sprawdzenie, czy gracz już istnieje w bazie danych
        if (!dataConfig.contains(playerPath)) {
            // Jeśli gracz nie istnieje, tworzymy bazową strukturę
            dataConfig.createSection(playerPath);
            dataConfig.createSection(playerPath + ".usedPools"); // Pusta sekcja dla przyszłych pul kodów
        }

        // Ustawienie lub aktualizacja czasu wygaśnięcia
        dataConfig.set(expirationPath, time);

        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: setExpiryTime: calling DataManager.saveData()");
        saveData();
    }


    // Remove the data for the given UUID.
    public void removePlayerData(UUID uuid) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: removePlayerData: called");
        dataConfig.set(uuid.toString(), null);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: removePlayerData: calling DataManager.saveData()");
        saveData();
    }

    // Get all UUIDs stored in the database.
    public Set<String> getAllPlayerUUIDs() {
        return dataConfig.getKeys(false);
    }
}
