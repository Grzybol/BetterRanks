package betterbox.mine.game.betterranks;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "DataManager: getPoolNameForCode called");

        // Przejście przez wszystkie klucze w konfiguracji
        for (String poolName : codesConfig.getKeys(false)) {
            // Sprawdzenie, czy dany pool zawiera kod
            if (codesConfig.contains(poolName + ".code") && codesConfig.getString(poolName + ".code").equals(code)) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "DataManager: getPoolNameForCode: Pool found for code "+code+" "+poolName);
                return poolName; // Zwrócenie nazwy poola, jeśli znaleziono kod
            }
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "DataManager: getPoolNameForCode: no pool found for code "+code);
        return null; // Zwróć null, jeśli kod nie istnieje
    }

    // Generate and store unique codes
    public void generateCodes(int maxUsers,String rank, int timeAmount, char timeUnit, String poolName) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: generateCodes called with parameters: "+rank+" "+timeAmount+" "+timeUnit+" "+poolName);
        String code;
        code = generateRandomCode(8);
        codesConfig.set(poolName + ".code", code);
        codesConfig.set(poolName + ".rank", rank);
        codesConfig.set(poolName + ".timeAmount", timeAmount);
        codesConfig.set(poolName + ".timeUnit", String.valueOf(timeUnit));
        codesConfig.set(poolName + ".maxUsers", maxUsers);
        codesConfig.set(poolName + ".currentUsers", 0);
        codesConfig.createSection(poolName + ".users");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: generateCodes: calling saveCodes()");
        saveCodes();
    }
    public String getCodeFromPool(String poolName){
        if (codesConfig.contains(poolName)) {
            return codesConfig.getString(poolName+ ".code");
        }
        return null; // Zwróć null, jeśli kod nie istnieje
    }



    private String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789";
        String numbers = "123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length-1; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }
        code.append(numbers.charAt(random.nextInt(numbers.length())));
        return code.toString();
    }
    public FileConfiguration getCodesConfig() {
        return codesConfig;
    }
    public boolean checkCode(String code) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "DataManager: containsCode called");

        // Iteracja przez wszystkie poolName w konfiguracji
        for (String poolName : codesConfig.getKeys(false)) {
            // Sprawdzenie, czy dany pool zawiera podany kod
            if (codesConfig.contains(poolName + ".code") && codesConfig.getString(poolName + ".code").equalsIgnoreCase(code)) {
                return true; // Zwróć true, jeśli znaleziono kod
            }
        }

        return false; // Zwróć false, jeśli kod nie został znaleziony
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
        Player player = Bukkit.getPlayer(playerUuid);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: canUseCode called");

        poolName = getPoolNameForCode(code);

        // Sprawdzamy, czy gracz już użył kodu z tej pule
        usedPoolsPath = poolName+".users."+player.getName();
        if (codesConfig.contains(usedPoolsPath)) {

            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"Player "+getOnlinePlayerNameByUUID(playerUuid)+" already used a code "+code+" from "+getPoolNameForCode(code)+" pool");
            return false; // Gracz już użył kodu z tej pule
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: canUseCode true");
        return true;
    }
    public boolean useCode(UUID playerUuid, String code) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: useCode called with parameters "+playerUuid+" "+code);
            usedPoolsPath = getPoolNameForCode(code)+".users."+getOnlinePlayerNameByUUID(playerUuid);
            codesConfig.set(usedPoolsPath,true);
            int currentUsers = getCodesConfig().getInt(getPoolNameForCode(code) + ".currentUsers");
            int maxUsers = getCodesConfig().getInt(getPoolNameForCode(code) + ".maxUsers");
            if(currentUsers+1==maxUsers){
                pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+getOnlinePlayerNameByUUID(playerUuid)+" used a code "+code+" from "+getPoolNameForCode(code)+" pool. Current users: "+currentUsers+", max users "+maxUsers );
                pluginLogger.log(PluginLogger.LogLevel.WARNING,"MAX USES REACHED! DELETING THE CODE FROM POOL "+getPoolNameForCode(code));
                codesConfig.set(poolName, null);
            }else{
                codesConfig.set(poolName+".currentUsers",currentUsers+1);
                pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+getOnlinePlayerNameByUUID(playerUuid)+" used a code "+code+" from "+getPoolNameForCode(code)+" pool. Current users: "+currentUsers+"."+(currentUsers+1)+", max users "+maxUsers );

            }
            // Zapisujemy zmiany
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: useCode: calling saveCodes()");
            saveCodes();
            return true;
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
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveData called");
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
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"DataManager: getExpiryTime: called with parameters "+uuid);
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"DataManager: getExpiryTime: player "+player.getName());

        String expirationPath = player.getName() + ".expiration";
        if (dataConfig.contains(expirationPath)) {
            long expiration = dataConfig.getLong(expirationPath);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"DataManager: getExpiryTime: Expiration "+expiration+" for player "+player.getName());
            return expiration;
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"DataManager: getExpiryTime: Expiration -1 for player "+player.getName());
        return -1; // Zwraca -1, jeśli czas wygaśnięcia nie jest ustawiony
    }
    public long getOldExpiration(UUID uuid){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"DataManager: getOldExpiration called with parameters "+uuid);
        String player_name = Bukkit.getOfflinePlayer(uuid).getName();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"DataManager: getOldExpiration: Checking player "+player_name);
        long oldExpiration = 0;
        if (dataConfig.contains( player_name+ ".oldExpiration")) {
            oldExpiration = dataConfig.getLong(Bukkit.getOfflinePlayer(uuid).getName() + ".oldExpiration");
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"DataManager: getOldExpiration: oldExpiration: "+oldExpiration);
        return oldExpiration;
    }
    public String getOldRank(UUID uuid){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"DataManager: getOldRank called with parameters "+uuid);
        String oldRank = dataConfig.getString(Bukkit.getOfflinePlayer(uuid).getName()+".oldRank");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"DataManager: getOldRank: oldRank: "+oldRank);
        return oldRank;
    }
    public void saveOldRank(UUID uuid, long expiration, String rank){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveOldRank called with parameters uuid: "+uuid+" expiration: "+expiration+" rank: "+rank);
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (!dataConfig.contains(player.getName() + ".oldExpiration")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveOldRank: Creating oldExpiration and oldRank for player "+player.getName());
            dataConfig.createSection(player.getName() + ".oldExpiration");
            dataConfig.createSection(player.getName() + ".oldRank");
        }
        dataConfig.set(player.getName() +".oldExpiration", expiration);
        dataConfig.set(player.getName() +".oldRank" ,rank);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: saveOldRank: oldRank "+rank+" oldExpiration "+expiration+" set,calling saveData");
        saveData();

    }

    // Set the expiry time for the given UUID.
    public void setExpiryTime(UUID uuid, long time, String rank) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: setExpiryTime: called with parameters "+time+" "+player.getName()+" "+rank);


        String playerPath = player.getName();
        String expirationPath = player.getName() + ".expiration";

        // Sprawdzenie, czy gracz już istnieje w bazie danych
        if (!dataConfig.contains(playerPath)) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: setExpiryTime: database.yml doesn't contain "+player.getName()+". Creating new entry");
            // Jeśli gracz nie istnieje, tworzymy bazową strukturę
            dataConfig.createSection(playerPath);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: setExpiryTime: Entry for "+player.getName()+" created.");
            dataConfig.createSection(playerPath+".rank");

        }

        // Ustawienie lub aktualizacja czasu wygaśnięcia
        dataConfig.set(expirationPath, time);
        dataConfig.set(playerPath+".rank" ,rank);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: setExpiryTime: calling DataManager.saveData()");
        saveData();
    }


    // Remove the data for the given UUID.
    public void removePlayerData(UUID uuid) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: removePlayerData: called");
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);

        // Sprawdź, czy gracz istnieje, aby uniknąć NullPointer Exception
        if (player != null) {
            // Ustaw wartość expiration na -1 dla danego gracza
            dataConfig.set(player.getName(), null);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: removePlayerData: rank removed for player "+player.getName());
        } else {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "[ERROR] DataManager: removePlayerData: Player not found for UUID: " + uuid);
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: removePlayerData: calling DataManager.saveData()");
        saveData();
    }

    public void checkAndCleanUpPools() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: checkAndCleanUpPools: called");

        // Przechodzimy przez wszystkich graczy w bazie danych
        for (String playerName : dataConfig.getKeys(false)) {
            try{
                String playerPath = playerName + ".usedPools";
                if (dataConfig.contains(playerPath)) {
                    Set<String> usedPools = dataConfig.getConfigurationSection(playerPath).getKeys(false);

                    for (String pool : usedPools) {
                        boolean poolHasCodes = false;

                        // Sprawdzamy, czy jakiekolwiek kody z tego pool'a są jeszcze dostępne
                        for (String code : codesConfig.getKeys(false)) {
                            if (codesConfig.getString(code + ".pool").equals(pool)) {
                                poolHasCodes = true;
                                break;
                            }
                        }

                        // Jeśli pool nie ma już dostępnych kodów, usuwamy go z listy użytych pooli gracza
                        if (!poolHasCodes) {
                            dataConfig.set(playerPath + "." + pool, null);
                            pluginLogger.log(PluginLogger.LogLevel.INFO, "DataManager: checkAndCleanUpPools: No available codes in pool " + pool + " for player " + playerName + ". Pool removed.");
                        }
                    }
                }
            }catch (Exception e) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG, "[ERROR] DataManager: checkAndCleanUpPools: Data saved after pool cleanup");
            }
        }

        // Zapisujemy zmiany w pliku konfiguracyjnym
        saveData();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: checkAndCleanUpPools: Data saved after pool cleanup");
    }


    // Get all UUIDs stored in the database.
    public Set<String> getAllPlayerNicknamesFromDB() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"DataManager: getAllPlayerNicknamesFromDB called");
        return dataConfig.getKeys(false);
    }
}
