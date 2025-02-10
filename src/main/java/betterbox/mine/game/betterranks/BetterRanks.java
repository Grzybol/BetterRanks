package betterbox.mine.game.betterranks;

import org.betterbox.elasticBuffer.ElasticBuffer;
import org.betterbox.elasticBuffer.ElasticBufferAPI;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class BetterRanks extends JavaPlugin {

    // Fields are made package-private or public to be accessible from the command handler
    File dataFile;
    File usersFile;
    PluginLogger pluginLogger;
    String folderPath;

    FileConfiguration dataConfig;
    FileConfiguration gmUsersConfig;
    DataManager dataManager;
    ConfigManager configManager;
    private Lang lang;

    @Override
    public void onEnable() {

        int pluginId = 22750; // Zamień na rzeczywisty ID twojego pluginu na bStats
        Metrics metrics = new Metrics(this, pluginId);
        folderPath =getDataFolder().getAbsolutePath();

        try{
            Set<PluginLogger.LogLevel> defaultLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.DEBUG, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            pluginLogger = new PluginLogger(getDataFolder().getAbsolutePath(), defaultLogLevels,this);
            loadElasticBuffer();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterRanks: onEnable: calling ConfigManager");
        }catch (Exception e){
            getServer().getLogger().warning("PluginLogger Exception: " + e.getMessage());
        }
        try{
        configManager = new ConfigManager(this, pluginLogger, folderPath);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterRanks: onEnable: calling DataManager");
        lang = new Lang(this, pluginLogger);
    }catch (Exception e){
        getServer().getLogger().warning("configManager Exception: " + e.getMessage());
    }
        dataManager = new DataManager(this, pluginLogger,lang);

        // Set the command executor to the new command handler class
        this.getCommand("br").setExecutor(new BetterRanksCommandHandler(this,pluginLogger,configManager,lang));
        pluginLogger.log(PluginLogger.LogLevel.INFO,"Plugin has been enabled!");

        // Load the users.yml file relative to the plugins directory
        dataFile = new File(getDataFolder(), "database.yml");
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        usersFile = new File(getDataFolder().getParentFile(), "GroupManager/worlds/world/users.yml");
        if (!usersFile.exists()) {
            pluginLogger.log(PluginLogger.LogLevel.WARNING,"No GroupManager users.yml file detected, creating a new one..");
            try {

                usersFile.getParentFile().mkdirs();
                usersFile.createNewFile();
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: onEnable: GroupManager users.yml file created.");
            } catch (IOException e) {
                e.printStackTrace();
                pluginLogger.log(PluginLogger.LogLevel.ERROR,"Could not create users.yml: " + e.getMessage());
            }
        }
        gmUsersConfig = YamlConfiguration.loadConfiguration(usersFile);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: onEnable: starting scheduler");
        // Load data from DataManager
        dataManager.reloadData();

        // Schedule a repeating task for rank expiry check
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: onEnable: starting scheduler");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::checkRankExpiry, 0L, 1200L);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: onEnable: scheduler started");
        getLogger().info("Enabled");
        getLogger().info(getDescription().getDescription());
        getLogger().info("Author: "+getDescription().getAuthors());
        getLogger().info("Version: "+getDescription().getVersion());

    }
    private void loadElasticBuffer(){
        try{
            PluginManager pm = Bukkit.getPluginManager();
            try {
                // Opóźnienie o 5 sekund, aby dać ElasticBuffer czas na pełną inicjalizację
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                pluginLogger.log(PluginLogger.LogLevel.WARNING, "[BetterElo] Initialization delay interrupted: " + e.getMessage());
                Thread.currentThread().interrupt(); // Przywrócenie statusu przerwania wątku
            }
            ElasticBuffer elasticBuffer = (ElasticBuffer) pm.getPlugin("ElasticBuffer");
            pluginLogger.isElasticBufferEnabled=true;
            pluginLogger.api= new ElasticBufferAPI(elasticBuffer);
        }catch (Exception e){
            pluginLogger.log(PluginLogger.LogLevel.ERROR, "ElasticBufferAPI instance found via ServicesManager, exception: "+e.getMessage());
        }
    }

    private void checkRankExpiry() {
        List<String> names = new ArrayList<>();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry called");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry: calling dataManager.getAllPlayerNicknamesFromDB()");

        for (String playerName : dataManager.getAllPlayerNicknamesFromDB()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"BetterRanks: checkRankExpiry: now checking "+playerName+" from database");
            names.add(playerName);
            try {
                // Zamiast UUID.fromString(uuidStr), użyj Bukkit.getOfflinePlayer(playerName)
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                UUID uuid = player.getUniqueId();
                //UUID.fromString()

                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"BetterRanks: checkRankExpiry: calling dataManager.getExpiryTime(player.getUniqueId()) with parameters "+player.getUniqueId()+" Player name: "+player.getName());
                long expiryTime = dataManager.getExpiryTime(player.getUniqueId());
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "BetterRanks: checkRankExpiry: expiryTime "+expiryTime);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "BetterRanks: checkRankExpiry: time left: "+(expiryTime-System.currentTimeMillis()));

                if (expiryTime != -1 && System.currentTimeMillis() > expiryTime) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"BetterRanks: checkRankExpiry: Rank expired for "+playerName);
                    long oldExpiration = dataManager.getOldExpiration(uuid);
                    if(oldExpiration>System.currentTimeMillis()){
                        String oldRank= dataManager.getOldRank(uuid);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"BetterRanks: checkRankExpiry: oldRank "+oldRank+" is still valid for player "+playerName);
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"BetterRanks: checkRankExpiry: calling /manuadd "+playerName+" "+oldRank+" world");
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manuadd " + playerName + " " + oldRank + " world");
                        dataManager.setExpiryTime(uuid,oldExpiration, oldRank);
                    }else {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterRanks: checkRankExpiry: " + playerName + " expired, removing rank");
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "BetterRanks: checkRankExpiry: calling removePlayerRank(player.getUniqueId())");
                        removePlayerRank(player.getUniqueId());
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");
                    }
                }
            } catch (Exception e) {

                pluginLogger.log(PluginLogger.LogLevel.ERROR, "BetterRanks: checkRankExpiry: Loop exception: " + e.getMessage()+". Player: "+playerName);
            }
        }
        String wszystkieNicki = String.join(", ", names);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterRanks: checkRankExpiry: Checked players: "+wszystkieNicki);

    }

    void removePlayerRank(UUID playerUUID) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: removePlayerRank: called");
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: removePlayerRank: calling dataManager.removePlayerData(playerUUID);");
        dataManager.removePlayerData(playerUUID);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: removePlayerRank: Player " + player.getName() + " removed from database.yml");
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manudel "+player.getName());


    }
    public String getCurrentRank(OfflinePlayer player){
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: getCurrentRank called with parameters "+player.getName());
        FileConfiguration gmUsersConfig;
        gmUsersConfig = YamlConfiguration.loadConfiguration(usersFile);
        String currentRank =gmUsersConfig.getString("users." + player.getUniqueId() + ".group");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: getCurrentRank return "+currentRank);
        return currentRank;
    }
    void  addPlayerRank(String playerName, String rank, int time, char timeUnit) {
        try {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank called");
            UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: UID "+playerUUID);
            //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: currentRank "+getCurrentRank(player));
            long additionalTime;
            switch (timeUnit) {
                case 's':
                    additionalTime = time * 1000L;
                    break;
                case 'm':
                    additionalTime = time * 60000L;
                    break;
                case 'h':
                    additionalTime = time * 3600000L;
                    break;
                case 'd':
                    additionalTime = time * 86400000L;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid time unit: " + timeUnit);
            }
            long currentExpiryTime=0;
            long expiryTime = System.currentTimeMillis();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: checking if player has a rank");
            String currentRank = getCurrentRank(player);
            if (rank.equals(currentRank)) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: Player "+playerName+" already has "+rank+" rank, extending for next "+additionalTime);
                currentExpiryTime = dataManager.getExpiryTime(playerUUID);
                expiryTime = Math.max(expiryTime, currentExpiryTime) + additionalTime;
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: extended");
            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: Player has a different rank, overriding. Additional Time "+additionalTime);
                expiryTime += additionalTime;
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: new expiryTime "+expiryTime);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: calling /manuadd "+playerName+" "+rank+" world");
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manuadd " + playerName + " " + rank + " world");
                currentExpiryTime = dataManager.getExpiryTime(playerUUID);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: new expiryTime "+expiryTime+" currentExpiryTime "+currentExpiryTime);
                if(currentExpiryTime>expiryTime){
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: calling saveOldRank with parameters playerUUID: "+playerUUID+" currentExpiryTime: "+currentExpiryTime+" currentRank: "+currentRank);
                    dataManager.saveOldRank(playerUUID, currentExpiryTime, currentRank);
                }

            }


            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: calling /manload");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: calling setExpiryTime with parameters: "+playerUUID+" "+expiryTime+" rank "+rank);
            dataManager.setExpiryTime(playerUUID, expiryTime, rank);
            pluginLogger.log(PluginLogger.LogLevel.INFO,"Rank "+rank+" updated successfully for " + playerName);
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanks: addPlayerRank: " + e.getMessage());
        }
    }



    @Override
    public void onDisable() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanks: onDisable: " + e.getMessage());
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: onDisable: calling dataManager.saveData()" );
        dataManager.saveData();
    }
}
