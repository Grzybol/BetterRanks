package betterbox.mine.game.betterranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BetterRanksCommandHandler implements CommandExecutor {

    private final BetterRanks plugin;
    public String poolName = null;
    private final PluginLogger pluginLogger;
    private final ConfigManager configManager;


    public BetterRanksCommandHandler(BetterRanks plugin,PluginLogger pluginLogger, ConfigManager configManager) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler called");
        this.plugin = plugin;
        this.pluginLogger = pluginLogger;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: onCommand called");

        if (!command.getName().equalsIgnoreCase("br")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage("Invalid command usage. Check the command syntax.");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "info":
                return handleInfoCommand(sender);
            case "tl":
                return handleTlCommand(sender);
            case "code":
                return handleCodeCommand(sender, args);
            case "createcode":
                return handleCreateCodeCommand(sender, args);
            case "delete":
                return handleDeleteCommand(sender, args);
            case "add":
                return handleAddCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);

            default:
                sender.sendMessage("Invalid command usage. Check the command syntax.");
                return true;
        }
    }
    private boolean handleReloadCommand(CommandSender sender){
        if(sender.hasPermission("betterranks.command.reload")){
            configManager.ReloadConfig();
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " BetterRanks config reloaded!");
            return true;
        }else {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleTlCommand: sender " + sender + " dont have permission to use /br tl");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.DARK_RED +"You don't have permission to use this command!");
            return false;
        }
    }

    // Metody obsługujące różne podkomendy
    private boolean handleInfoCommand(CommandSender sender) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleInfoCommand called");
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Better Ranks system for BetterBox.");
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Author: " + plugin.getDescription().getAuthors());
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Version: " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br code <code> " + ChatColor.GREEN + " - to use promo code");
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br tl " + ChatColor.GREEN + " - returns time left on your rank");

        if(sender.isOp()){
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleInfoCommand: "+sender.getName()+" is OP "+sender.isOp());
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br delete <nick> " + ChatColor.GREEN + " - set player's rank to Player");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br add <nick> <rank> <time_amount> <s/m/d> " + ChatColor.GREEN + " - set player's rank for given time");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br createcode <quantity> <rank> <time_amount> <s/m/d> <pool_name> " + ChatColor.GREEN + " - create codes for ranks under given pool name. Each user can redeem only one code per pool.");
        }
        return true;
    }

    private boolean handleTlCommand(CommandSender sender) {
        if(sender.hasPermission("betterranks.command.tl")){
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleTlCommand called");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleTlCommand: sender: " + sender + " sender.getName(): " + sender.getName());
            if (sender.hasPermission("betterranks.command.tl")) {
                UUID uuid = null;
                try {
                    uuid = Objects.requireNonNull(Bukkit.getPlayer(sender.getName())).getUniqueId();
                } catch (Exception e) {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanksCommandHandler: handleTlCommand: exception while converting username to UUID: " + e.getMessage() + " " + e);
                }
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleTlCommand: calling getRemainingTimeFormatted with " + uuid);
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.AQUA + plugin.dataManager.getRemainingTimeFormatted(uuid));
            } else {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleTlCommand: sender " + sender + " dont have permission to use /br tl");
                sender.sendMessage("You don't have permission to use this command!");
            }
            return true;
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleTlCommand: sender " + sender + " dont have permission to use /br tl");
        return false;
    }

    private boolean handleCodeCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: onCommand: /br code called");
                try {
                    if (sender.hasPermission("betterranks.command.code")) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: onCommand: calling handleCodeUsageCommand with parameters: " + sender + " " + args[1]);
                        return handleCodeUsageCommand(sender, args[1]);
                    } else {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: onCommand: sender " + sender + " dont have permission to use /br code");
                        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.DARK_RED +"You don't have permission to use this command!");
                    }
                } catch (Exception e) {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanksCommandHandler: onCommand: exception while checking permissions: " + e.getMessage() + " " + e);
                }
            }
        }
        return false;
    }

    private boolean handleAddCommand(CommandSender sender, String[] args) {
        if(sender.hasPermission("betterranks.command.add")){
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleAddCommand called with parameters " + sender.getName() + " " + Arrays.toString(args));
            try {
                String playerName = args[1];
                String rank = args[2];
                int amount = Integer.parseInt(args[3]);
                char timeUnit = args[4].charAt(0); // s for seconds, m for minutes, d for days
                plugin.addPlayerRank(playerName, rank, amount, timeUnit);
                sender.sendMessage("Rank added successfully for player " + playerName);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid format for amount. Usage: /br add <player> <rank> <amount> <s/m/d> " + e.getMessage());
                return true;
            }
        }
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.DARK_RED +"You don't have permission to use this command!");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleAddCommand: sender " + sender + " dont have permission to use /br tl");

        return false;
    }


    private boolean handleCreateCodeCommand(CommandSender sender, String[] args) {
        if(sender.hasPermission("betterranks.command.createcode")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCreateCodeCommand called by " + sender.getName() + " " + Arrays.toString(args));
            try {
                String maxUsers = args[1];
                String rank = args[2];
                int timeAmount = Integer.parseInt(args[3]);
                char timeUnit = args[4].charAt(0); // s for seconds, m for minutes, d for days
                poolName = args[5];

                plugin.dataManager.generateCodes(Integer.parseInt(maxUsers), rank, timeAmount, timeUnit, poolName);
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Code "+ChatColor.GOLD + "" + ChatColor.DARK_RED +plugin.dataManager.getCodeFromPool(poolName)+" "+ ChatColor.AQUA + " created successfully.");
                sender.sendMessage("Generated " + plugin.dataManager.getCodeFromPool(poolName)+ " code successfully.");
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid number format. Usage: /br createcode <maxUsers> <rank> <amount_of_time> <time_unit> "+e.getMessage());
                return true;
            }
        }
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.DARK_RED +"You don't have permission to use this command!");
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCreateCodeCommand: sender " + sender.getName() + " dont have permission to use /br code");
        return false;
    }

    private boolean handleCodeUsageCommand(CommandSender sender, String code) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCodeUsageCommand called");


        if (plugin.dataManager.checkCode(code)) {
            Player player = (Player) sender;
            String rankFromCode = plugin.dataManager.getCodesConfig().getString(plugin.dataManager.getPoolNameForCode(code) + ".rank");

            if (isCurrentRankHigher(player, rankFromCode)) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.DARK_RED +"You already have a higher or equal rank.");
                return true;
            }
            UUID playerUUID = player.getUniqueId();
            // Pobierz szczegóły kodu
            String playerName = sender.getName();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCodeUsageCommand: playerName "+playerName+" for code "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code));
            String rank = plugin.dataManager.getCodesConfig().getString( plugin.dataManager.getPoolNameForCode(code)+ ".rank");
            int timeAmount = plugin.dataManager.getCodesConfig().getInt(plugin.dataManager.getPoolNameForCode(code) + ".timeAmount");
            String timeUnitStr = plugin.dataManager.getCodesConfig().getString(plugin.dataManager.getPoolNameForCode(code) + ".timeUnit");
            String maxUsers = plugin.dataManager.getCodesConfig().getString(plugin.dataManager.getPoolNameForCode(code) + ".maxUsers");
            String currentUsers = plugin.dataManager.getCodesConfig().getString(plugin.dataManager.getPoolNameForCode(code) + ".currentUsers");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler> handleCodeUsageCommand> rank: "+rank+" timeAmount: "+timeAmount+" timeUnit: "+timeUnitStr+" maxUsers: "+maxUsers+" currentUsers: "+currentUsers);
            char timeUnit; // Domyślna wartość, jeśli konwersja zawiedzie
            //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCodeUsageCommand: calling plugin.dataManager.checkCode(code) "+ code);
            if (timeUnitStr.length() == 1) {
                    timeUnit = timeUnitStr.charAt(0);
            } else {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanksCommandHandler: handleCodeUsageCommand: Invalid time unit format in config "+timeUnitStr);
                    throw new IllegalArgumentException("Invalid time unit format");


            }


            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCodeUsageCommand: calling dataManager.canUseCode(code) "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code));
            if(!plugin.dataManager.canUseCode(playerUUID,code)){
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.DARK_RED +"You already used a code from that pool!");
                return false;
            }else{
                //int maxUsers = plugin.dataManager.getCodesConfig().getInt(plugin.dataManager.getPoolNameForCode(code) + ".maxUsers");
                pluginLogger.log(PluginLogger.LogLevel.INFO,"Code "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code)+" used successfully by "+playerName+ ". Rank " + rank + " added/extended for next " + timeAmount + timeUnitStr.charAt(0) + ".");
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Code used successfully. Rank "+ ChatColor.BOLD + rank +ChatColor.BOLD+" added for "+ChatColor.BOLD+ timeAmount + timeUnit +ChatColor.BOLD+ ".");
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCodeUsageCommand: calling addPlayerRank with parameters "+playerName+","+rank+","+timeAmount+","+timeUnitStr.charAt(0));
                plugin.addPlayerRank(playerName, rank, timeAmount, timeUnitStr.charAt(0));
                plugin.dataManager.useCode(playerUUID,code);
            }

        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA +" Invalid or expired code.");
            pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+sender+" used wrong or expired code "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code));

        }
        return true;
    }
    private boolean isCurrentRankHigher(Player player, String newRank) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: isCurrentRankHigher called with parameters "+player.getName()+" "+newRank);
        String currentRank = plugin.getCurrentRank(player);
        Map<Integer, String> hierarchy = configManager.getRankHierarchy();

        // Znajdź pozycje obecnej rangi i nowej rangi w hierarchii
        int currentRankPosition = hierarchy.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(currentRank))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(-1); // -1, jeśli ranga nie została znaleziona

        int newRankPosition = hierarchy.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(newRank))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(-1);
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: isCurrentRankHigher called with parameters "+player.getName()+" new rank: "+currentRankPosition+" old rank: "+newRankPosition);
        // Porównanie pozycji rang
        return currentRankPosition > newRankPosition;
    }

    private boolean handleDeleteCommand(CommandSender sender, String[] args) {

        if(sender.hasPermission("betterranks.command.delete")){
            String playerName = args[1];
            UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId(); // Używaj getOfflinePlayer zamiast getPlayer
            plugin.removePlayerRank(uuid);
            pluginLogger.log(PluginLogger.LogLevel.INFO,"BetterRanksCommandHandler: onCommand: Player " + playerName + " removed");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");

            sender.sendMessage("Rank removed successfully for player " + playerName);
            return true;
        }
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleDeleteCommand: sender " + sender + " dont have permission to use /br delete");
        return false;
    }

}
