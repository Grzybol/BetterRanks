package betterbox.mine.game.betterranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class BetterRanksCommandHandler implements CommandExecutor {

    private BetterRanks plugin = null;
    public String poolName = null;


    public BetterRanksCommandHandler(BetterRanks plugin) {

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand called");

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
            default:
                sender.sendMessage("Invalid command usage. Check the command syntax.");
                return true;
        }
    }

    // Metody obsługujące różne podkomendy
    private boolean handleInfoCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Better Ranks system for BetterBox.");
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Author: " + plugin.getDescription().getAuthors());
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Version: " + plugin.getDescription().getVersion());
        return true;
    }

    private boolean handleTlCommand(CommandSender sender) {
        if(sender.hasPermission("betterranks.command.tl")){
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleTlCommand: /br tl called");
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleTlCommand: sender: " + sender + " sender.getName(): " + sender.getName() + " " + sender.toString());
            if (sender.hasPermission("betterranks.command.tl")) {
                UUID uuid = null;
                try {
                    uuid = Bukkit.getPlayer(sender.getName()).getUniqueId();
                } catch (Exception e) {
                    plugin.pluginLogger.error("BetterRanksCommandHandler: handleTlCommand: exception while converting username to UUID: " + e.getMessage() + " " + e);
                }
                plugin.pluginLogger.debug("BetterRanksCommandHandler: handleTlCommand: calling getRemainingTimeFormatted with " + uuid);
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.AQUA + plugin.dataManager.getRemainingTimeFormatted(uuid));
            } else {
                plugin.pluginLogger.debug("BetterRanksCommandHandler: handleTlCommand: sender " + sender + " dont have permission to use /br tl");
                sender.sendMessage("You don't have permission to use this command!");
            }
            return true;
        }
        plugin.pluginLogger.debug("BetterRanksCommandHandler: handleTlCommand: sender " + sender + " dont have permission to use /br tl");
        return false;
    }

    private boolean handleCodeCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            {
                plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand: /br code called");
                try {
                    if (sender.hasPermission("betterranks.command.code")) {
                        plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand: calling handleCodeUsageCommand with parameters: " + sender + " " + args[1]);
                        return handleCodeUsageCommand(sender, args[1]);
                    } else {
                        plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand: sender " + sender + " dont have permission to use /br code");
                        sender.sendMessage("You don't have permission to use this command!");
                    }
                } catch (Exception e) {
                    plugin.pluginLogger.error("BetterRanksCommandHandler: onCommand: exception while checking permissions: " + e.getMessage() + " " + e);
                }
            }
        }
        return false;
    }

    private boolean handleAddCommand(CommandSender sender, String[] args) {
        if(sender.hasPermission("betterranks.command.add")){
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleAddCommand called with parameters " + sender.getName() + " " + Arrays.toString(args));
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
        plugin.pluginLogger.debug("BetterRanksCommandHandler: handleAddCommand: sender " + sender + " dont have permission to use /br tl");

        return false;
    }


    private boolean handleCreateCodeCommand(CommandSender sender, String[] args) {
        if(sender.hasPermission("betterranks.command.createcode")) {
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCreateCodeCommand called by " + sender.getName() + " " + Arrays.toString(args));
            try {
                int numberOfCodes = Integer.parseInt(args[1]);
                String rank = args[2];
                int timeAmount = Integer.parseInt(args[3]);
                char timeUnit = args[4].charAt(0); // s for seconds, m for minutes, d for days
                poolName = args[5];

                plugin.dataManager.generateCodes(numberOfCodes, rank, timeAmount, timeUnit, poolName);
                sender.sendMessage("Generated " + numberOfCodes + " codes successfully.");
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid number format. Usage: /br createcode <number_of_codes> <rank> <amount_of_time> <time_unit> "+e.getMessage());
                return true;
            }
        }
        plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCreateCodeCommand: sender " + sender.getName() + " dont have permission to use /br code");
        return false;
    }

    private boolean handleCodeUsageCommand(CommandSender sender, String code) {
        plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand called");


        if (plugin.dataManager.checkCode(code)) {
            Player player = (Player) sender;
            UUID playerUUID = player.getUniqueId();
            // Pobierz szczegóły kodu
            String playerName = sender.getName();
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: playerName "+playerName+" for code "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code));

            String rank = plugin.dataManager.getCodesConfig().getString(code + ".rank");

            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: rank "+rank+" for code "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code));
            int timeAmount = plugin.dataManager.getCodesConfig().getInt(code + ".timeAmount");
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: timeAmount "+timeAmount+" for code "+code);
            String timeUnitStr = plugin.dataManager.getCodesConfig().getString(code + ".timeUnit");
            char timeUnit = timeUnitStr.charAt(0);; // Domyślna wartość, jeśli konwersja zawiedzie
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: calling plugin.dataManager.checkCode(code) "+ code);
            if (timeUnitStr != null && timeUnitStr.length() == 1) {
                    timeUnit = timeUnitStr.charAt(0);
            } else {
                    plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: Invalid time unit format "+timeUnitStr);
                    throw new IllegalArgumentException("Invalid time unit format");

            }

            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: timeUnit "+timeUnitStr);
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: Player " + playerName + " , rank: "+rank+" , time: "+timeAmount+" , unit: "+timeUnitStr.charAt(0)+" pool "+plugin.dataManager.getPoolNameForCode(code));
            // Zakładając, że 'sender' jest graczem

            // Dodaj rangę graczowi
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: calling addPlayerRank with parameters "+playerName+","+rank+","+timeAmount+","+timeUnitStr.charAt(0));
            plugin.addPlayerRank(playerName, rank, timeAmount, timeUnitStr.charAt(0));
            plugin.pluginLogger.info("Code "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code)+" used successfully by "+playerName+ ". Rank " + rank + " added/extended for next" + timeAmount + timeUnitStr.charAt(0) + ".");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Code used successfully. Rank " + rank + " added for " + timeAmount + timeUnit + ".");
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: calling dataManager.useCode(code) "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code));
            plugin.dataManager.useCode(playerUUID,code);
        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA +" Invalid or expired code.");
            plugin.pluginLogger.info("Player "+sender+" used wrong or expired code "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code));

        }
        return true;
    }


    private boolean handleDeleteCommand(CommandSender sender, String[] args) {
        if(sender.hasPermission("betterranks.command.delete")){
            String playerName = args[1];
            UUID uuid = Bukkit.getPlayer(playerName).getUniqueId();
            plugin.removePlayerRank(uuid);
            plugin.pluginLogger.info("BetterRanksCommandHandler: onCommand: Player " + playerName + " removed");
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");

            sender.sendMessage("Rank removed successfully for player " + playerName);
            return true;
        }
        plugin.pluginLogger.debug("BetterRanksCommandHandler: handleDeleteCommand: sender " + sender + " dont have permission to use /br delete");
        return false;
    }
}
