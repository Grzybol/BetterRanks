package betterbox.mine.game.betterranks;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class BetterRanks extends JavaPlugin {

    // Fields are made package-private or public to be accessible from the command handler
    File usersFile;
    PluginLogger pluginLogger;
    FileConfiguration usersConfig;
    DataManager dataManager;

    @Override
    public void onEnable() {
        pluginLogger = new PluginLogger(this);
        dataManager = new DataManager(this, pluginLogger);

        // Set the command executor to the new command handler class
        this.getCommand("br").setExecutor(new BetterRanksCommandHandler(this));
        pluginLogger.info("Plugin has been enabled!");

        // Load the users.yml file relative to the plugins directory
        usersFile = new File(getDataFolder().getParentFile(), "GroupManager/worlds/world/users.yml");
        if (!usersFile.exists()) {
            try {
                usersFile.getParentFile().mkdirs();
                usersFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                pluginLogger.severe("Could not create users.yml: " + e.getMessage());
            }
        }
        usersConfig = YamlConfiguration.loadConfiguration(usersFile);

        // Load data from DataManager
        dataManager.reloadData();

        // Schedule a repeating task for rank expiry check
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::checkRankExpiry, 0L, 72000L);
    }

    private void checkRankExpiry() {
        boolean updated = false;

        for (String uuidStr : dataManager.getAllPlayerUUIDs()) {
            long expiryTime = dataManager.getExpiryTime(UUID.fromString(uuidStr));

            if (expiryTime != -1 && System.currentTimeMillis() > expiryTime) {
                removePlayerRank(UUID.fromString(uuidStr));
                updated = true;
            }
        }

        if (updated) {
            try {
                usersConfig.save(usersFile);
            } catch (IOException e) {
                pluginLogger.severe("BetterRanks: checkRankExpiry: "+e);
            }
        }
    }

    void removePlayerRank(UUID playerUUID) {
        usersConfig.set("users." + playerUUID.toString() + ".group", "Player");
        dataManager.removePlayerData(playerUUID);
        pluginLogger.info("BetterRanks: removePlayerRank: Player " + playerUUID + " removed");
        try {
            usersConfig.save(usersFile);
            pluginLogger.info("Rank set successfully!");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");

        } catch (IOException e) {
            pluginLogger.severe("BetterRanks: removePlayerRank: "+e);
        }
    }

    void addPlayerRank(String playerName, String rank, int time, char timeUnit) {
        try {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manuadd " + playerName + " " + rank);

            // Get UUID from playerName
            UUID playerUUID = Bukkit.getPlayer(playerName).getUniqueId();

            long expiryTime = System.currentTimeMillis();
            switch (timeUnit) {
                case 's':
                    expiryTime += time * 1000L;
                    break;
                case 'm':
                    expiryTime += time * 60000L;
                    break;
                case 'd':
                    expiryTime += time * 86400000L;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid time unit: " + timeUnit);
            }

            // Set expiry time using UUID
            dataManager.setExpiryTime(playerUUID, expiryTime);
            pluginLogger.info("Rank added successfully for " + playerName);
        } catch (Exception e) {
            pluginLogger.severe("BetterRanks: addPlayerRank: " + e);
        }
    }


    @Override
    public void onDisable() {
        // Save the users file
        try {
            usersConfig.save(usersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save data to DataManager
        dataManager.saveData();
    }
}
