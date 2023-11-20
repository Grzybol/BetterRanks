package betterbox.mine.game.betterranks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public final class BetterRanks extends JavaPlugin {

    // Fields are made package-private or public to be accessible from the command handler
    File usersFile;
    PluginLogger pluginLogger;
    FileConfiguration usersConfig;
    DataManager dataManager;
    ConfigManager configManager;

    @Override
    public void onEnable() {
        try{
            Set<PluginLogger.LogLevel> defaultLogLevels = EnumSet.of(PluginLogger.LogLevel.INFO, PluginLogger.LogLevel.DEBUG, PluginLogger.LogLevel.WARNING, PluginLogger.LogLevel.ERROR);
            pluginLogger = new PluginLogger(getDataFolder().getAbsolutePath(), defaultLogLevels);
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
        this.getCommand("br").setExecutor(new BetterRanksCommandHandler(this,pluginLogger));
        pluginLogger.log(PluginLogger.LogLevel.INFO,"Plugin has been enabled!");

        // Load the users.yml file relative to the plugins directory
        usersFile = new File(getDataFolder().getParentFile(), "GroupManager/worlds/world/users.yml");
        if (!usersFile.exists()) {
            try {
                usersFile.getParentFile().mkdirs();
                usersFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                pluginLogger.log(PluginLogger.LogLevel.ERROR,"Could not create users.yml: " + e.getMessage());
            }
        }
        usersConfig = YamlConfiguration.loadConfiguration(usersFile);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: onEnable: starting scheduler");
        // Load data from DataManager
        dataManager.reloadData();

        // Schedule a repeating task for rank expiry check
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: onEnable: starting scheduler");
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::checkRankExpiry, 0L, 1200L);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: onEnable: scheduler started");
    }

    private void checkRankExpiry() {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry: starting scheduled task");

        boolean updated = false;
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry: requesting all UUIDs");
        for (String uuidStr : dataManager.getAllPlayerUUIDs()) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry: checking UUID "+uuidStr);
            long expiryTime = dataManager.getExpiryTime(UUID.fromString(uuidStr));
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry: expiry time for UUID "+expiryTime);

            if (expiryTime != -1 && System.currentTimeMillis() > expiryTime) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry: UUID expired, removing rank");
                removePlayerRank(UUID.fromString(uuidStr));
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanks: checkRankExpiry: changing users.yml GroupManager file");
                updated = true;
            }
        }

        if (updated) {
            try {
                usersConfig.save(usersFile);
            } catch (IOException e) {
                pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanks: checkRankExpiry: "+e);
            }
        }
    }

    void removePlayerRank(UUID playerUUID) {
        pluginLogger.log(PluginLogger.LogLevel.INFO,"BetterRanks: removePlayerRank: called");
        usersConfig.set("users." + playerUUID.toString() + ".group", "Player");
        dataManager.removePlayerData(playerUUID);
        pluginLogger.log(PluginLogger.LogLevel.INFO,"BetterRanks: removePlayerRank: Player " + playerUUID + " removed");
        try {
            usersConfig.save(usersFile);
            pluginLogger.log(PluginLogger.LogLevel.INFO,"Rank set successfully!");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");

        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanks: removePlayerRank: "+e);
        }
    }

    void addPlayerRank(String playerName, String rank, int time, char timeUnit) {
        try {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank called");
            // Get UUID from playerName

            UUID playerUUID = Bukkit.getOfflinePlayer(playerName).getUniqueId();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: UID "+playerUUID);
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
                case 'd':
                    additionalTime = time * 86400000L;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid time unit: " + timeUnit);
            }

            long expiryTime = System.currentTimeMillis();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: checking if player has a rank");
            // Check if player already has the same rank
            if (rank.equals(currentRank)) {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: Player "+playerName+" already has "+rank+" rank, extending for next "+additionalTime);
                long currentExpiryTime = dataManager.getExpiryTime(playerUUID);
                // Extend the expiry time if the player already has the rank
                expiryTime = Math.max(expiryTime, currentExpiryTime) + additionalTime;
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: extended");
            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: Player has a different rank, overriding. Additional Time "+additionalTime);
                // If the player has a different rank, set new expiry time
                expiryTime += additionalTime;
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: new expiryTime "+expiryTime);
                // Update the player's rank
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: calling /manuadd "+playerName+" "+rank+" world");
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manuadd " + playerName + " " + rank + " world");
                usersConfig.set("users." + playerUUID + ".group", rank);
                // Uncomment the line below if you wish to use the 'manuadd' command
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: calling /manload");

                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");

            }
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: addPlayerRank: calling setExpiryTime with parameters: "+playerUUID+" "+expiryTime);
            // Set or update the expiry time in DataManager
            dataManager.setExpiryTime(playerUUID, expiryTime);
            pluginLogger.log(PluginLogger.LogLevel.INFO,"Rank updated successfully for " + playerName);
        } catch (Exception e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanks: addPlayerRank: " + e.getMessage());
        }
    }



    @Override
    public void onDisable() {
        // Save the users file
        try {
            usersConfig.save(usersFile);
        } catch (IOException e) {
            pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanks: onDisable: " + e.getMessage());
        }

        // Save data to DataManager
        dataManager.saveData();
    }
}
