package betterbox.mine.game.betterranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BetterRanksCommandHandler implements CommandExecutor {

    private BetterRanks plugin = null;


    public BetterRanksCommandHandler(BetterRanks plugin) {

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand called");

        if (command.getName().equalsIgnoreCase("br")) {
            if(args.length == 1 && args[0].equals("info")){
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Better Ranks system for BetterBox.");
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Author: " + plugin.getDescription().getAuthors());
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Version: " + plugin.getDescription().getVersion());
            }
            if(args.length == 1 && args[0].equals("tl")) {
                plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand: /br tl called");
                plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand: sender: "+sender+" sender.getName(): "+sender.getName()+" "+sender.toString());
                if (sender.hasPermission("betterranks.command.tl")) {
                    UUID uuid = null;
                    try {
                        uuid = Bukkit.getPlayer(sender.getName()).getUniqueId();
                    }catch (Exception e)
                    {
                        plugin.pluginLogger.severe("BetterRanksCommandHandler: onCommand: exception while converting username to UUID: "+e.getMessage()+ " "+e);
                    }
                    plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand: calling getRemainingTimeFormatted with "+uuid);
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.AQUA + plugin.dataManager.getRemainingTimeFormatted(uuid));
                }else {
                    plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand: sender "+sender+" dont have permission to use /br tl");
                    sender.sendMessage("You don't have permission to use this command!");
                }
                return true;
            }

            // Handling the 'code' command, no permission required
            if (args.length == 2 && args[0].equals("code")) {
                plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand: /br code called");
                try {
                    if (sender.hasPermission("betterranks.command.code")) {
                        plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand: calling handleCodeUsageCommand with parameters: " + sender + " " + args[1]);
                        return handleCodeUsageCommand(sender, args[1]);
                    } else {
                        plugin.pluginLogger.debug("BetterRanksCommandHandler: onCommand: sender " + sender + " dont have permission to use /br code");
                        sender.sendMessage("You don't have permission to use this command!");
                    }
                }catch (Exception e)
                {
                    plugin.pluginLogger.severe("BetterRanksCommandHandler: onCommand: exception while checking permissions: "+e.getMessage()+ " "+e);
                }
            }


            // Check permission for other /br commands
            if (!sender.hasPermission("betterranks.command.br")) {
                sender.sendMessage("You don't have permission to use this command!");
                return true;
            }

            if (command.getName().equalsIgnoreCase("br")) {
                if(args.length == 1 && args[0].equals("debug")) {
                    plugin.pluginLogger.debugBboolStateChange();
                }
            }
            // Command for creating codes
            if (args.length >= 4 && args[0].equals("createcode")) {
                return handleCreateCodeCommand(sender, args);
            }

            // Command for deleting a player's rank
            if (args.length == 2 && args[0].equals("delete")) {
                return handleDeleteCommand(sender, args);
            }
            if (args.length == 5 && args[0].equals("add")) {
                try {
                    String playerName = args[1];
                    String rank = args[2];
                    int amount = Integer.parseInt(args[3]);
                    char timeUnit = args[4].charAt(0); // s for seconds, m for minutes, d for days
                    plugin.addPlayerRank(playerName, rank, amount, timeUnit);
                    sender.sendMessage("Rank added successfully for player " + playerName);
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid format for amount. Usage: /br add <player> <rank> <amount> <s/m/d>");
                    return true;
                }
            }

            sender.sendMessage("Invalid command usage. Check the command syntax.");
            return true;
        }
        return false;
    }

    private boolean handleCreateCodeCommand(CommandSender sender, String[] args) {
        try {
            int numberOfCodes = Integer.parseInt(args[1]);
            String rank = args[2];
            int timeAmount = Integer.parseInt(args[3]);
            char timeUnit = args[4].charAt(0); // s for seconds, m for minutes, d for days

            plugin.dataManager.generateCodes(numberOfCodes, rank, timeAmount, timeUnit);
            sender.sendMessage("Generated " + numberOfCodes + " codes successfully.");
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid number format. Usage: /br createcode <number_of_codes> <rank> <amount_of_time> <time_unit>");
            return true;
        }
    }

    private boolean handleCodeUsageCommand(CommandSender sender, String code) {
        plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand called");


        if (plugin.dataManager.checkCode(code)) {
            // Pobierz szczegóły kodu
            String playerName = sender.getName();
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: playerName "+playerName+" for code "+code);

            String rank = plugin.dataManager.getCodesConfig().getString(code + ".rank");

            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: rank "+rank+" for code "+code);
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
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: Player " + playerName + " , rank: "+rank+" , time: "+timeAmount+" , unit: "+timeUnitStr.charAt(0));
            // Zakładając, że 'sender' jest graczem

            // Dodaj rangę graczowi
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: calling addPlayerRank with parameters "+playerName+","+rank+","+timeAmount+","+timeUnitStr.charAt(0));
            plugin.addPlayerRank(playerName, rank, timeAmount, timeUnitStr.charAt(0));
            plugin.pluginLogger.info("Code used successfully. Rank " + rank + " added for " + timeAmount + timeUnitStr.charAt(0) + ".");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Code used successfully. Rank " + rank + " added for " + timeAmount + timeUnit + ".");
            plugin.pluginLogger.debug("BetterRanksCommandHandler: handleCodeUsageCommand: calling dataManager.useCode(code) "+code);
            plugin.dataManager.useCode(code);
        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA +" Invalid or expired code.");
            plugin.pluginLogger.info("Player "+sender+" used wrong or expired code");

        }
        return true;
    }


    private boolean handleDeleteCommand(CommandSender sender, String[] args) {
        String playerName = args[1];
        UUID uuid = Bukkit.getPlayer(playerName).getUniqueId();
        plugin.removePlayerRank(uuid);
        plugin.pluginLogger.info("BetterRanksCommandHandler: onCommand: Player " + playerName + " removed");
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");

        sender.sendMessage("Rank removed successfully for player " + playerName);
        return true;
    }
}
