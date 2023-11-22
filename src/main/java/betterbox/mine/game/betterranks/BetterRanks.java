package betterbox.mine.game.betterranks;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class BetterRanks extends JavaPlugin {

    // Fields are made package-private or public to be accessible from the command handler
    File dataFile;
    File usersFile;
    PluginLogger pluginLogger;

    FileConfiguration dataConfig;
    DataManager dataManager;
    ConfigManager configManager;

    @Override
    public void onEnable() {
        try{
            Set<PluginLogger.LogLevel> defaultLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.DEBUG, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            pluginLogger = new PluginLogger(getDataFolder().getAbsolutePath(), defaultLogLevels,this);
            pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterRanks: onEnable: calling ConfigManager");
        }catch (Exception e){
            getServer().getLogger().warning("PluginLogger Exception: " + e.getMessage());
        }
        try{
        configManager = new ConfigManager(this, pluginLogger);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterRanks: onEnable: calling DataManager");
    }catch (Exception e){
        getServer().getLogger().warning("configManager Exception: " + e.getMessage());
    }
        dataManager = new DataManager(this, pluginLogger);

        // Set the command executor to the new command handler class
        this.getCommand("br").setExecutor(new BetterRanksCommandHandler(this,pluginLogger,configManager));
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

    private void checkRankExpiry() {
        List<String> names = new ArrayList<>();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry called");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry: calling dataManager.checkAndCleanUpPools()");
        //dataManager.checkAndCleanUpPools();
        //boolean updated = false;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry: calling dataManager.getAllPlayerNicknamesFromDB()");

        for (String playerName : dataManager.getAllPlayerNicknamesFromDB()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"BetterRanks: checkRankExpiry: now checking "+playerName+" from database");
            names.add(playerName);
            try {
                // Zamiast UUID.fromString(uuidStr), użyj Bukkit.getOfflinePlayer(playerName)
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
                //UUID.fromString()

                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"BetterRanks: checkRankExpiry: calling dataManager.getExpiryTime(player.getUniqueId()) with parameters "+player.getUniqueId()+" Player name: "+player.getName());
                long expiryTime = dataManager.getExpiryTime(player.getUniqueId());
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "BetterRanks: checkRankExpiry: expiryTime "+expiryTime);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2, "BetterRanks: checkRankExpiry: time left: "+(expiryTime-System.currentTimeMillis()));

                if (expiryTime != -1 && System.currentTimeMillis() > expiryTime) {
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG, "BetterRanks: checkRankExpiry: " + playerName + " expired, removing rank");
                    pluginLogger.log(PluginLogger.LogLevel.DEBUG_LVL2,"BetterRanks: checkRankExpiry: calling removePlayerRank(player.getUniqueId())");
                    removePlayerRank(player.getUniqueId());
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");
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

    void addPlayerRank(String playerName, String rank, int time, char timeUnit) {
        try {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank called");
            UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: UID "+playerUUID);
            FileConfiguration usersConfig;
            usersConfig = YamlConfiguration.loadConfiguration(dataFile);
            String currentRank = usersConfig.getString("users." + playerUUID + ".group");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: currentRank "+currentRank);
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

            long expiryTime = System.currentTimeMillis();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: checking if player has a rank");
            if (rank.equals(currentRank)) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: Player "+playerName+" already has "+rank+" rank, extending for next "+additionalTime);
                long currentExpiryTime = dataManager.getExpiryTime(playerUUID);
                expiryTime = Math.max(expiryTime, currentExpiryTime) + additionalTime;
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: extended");
            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: Player has a different rank, overriding. Additional Time "+additionalTime);
                expiryTime += additionalTime;
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: new expiryTime "+expiryTime);
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: calling /manuadd "+playerName+" "+rank+" world");
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manuadd " + playerName + " " + rank + " world");
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: calling /manload");

                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");

            }
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: calling setExpiryTime with parameters: "+playerUUID+" "+expiryTime);
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
