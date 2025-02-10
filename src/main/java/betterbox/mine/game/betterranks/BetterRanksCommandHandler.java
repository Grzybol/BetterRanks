package betterbox.mine.game.betterranks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class BetterRanksCommandHandler implements CommandExecutor, TabCompleter {

    private final BetterRanks plugin;
    public String poolName = null;
    private final PluginLogger pluginLogger;
    private final ConfigManager configManager;
    private final Lang lang;


    public BetterRanksCommandHandler(BetterRanks plugin,PluginLogger pluginLogger, ConfigManager configManager, Lang lang) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler called");
        this.plugin = plugin;
        this.lang = lang;
        this.pluginLogger = pluginLogger;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String transactionID = UUID.randomUUID().toString();
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: onCommand called", transactionID);

        if (!command.getName().equalsIgnoreCase("br")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.RED +lang.invalidCommand);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "info":
                return handleInfoCommand(sender,transactionID);
            case "help":
                return handleInfoCommand(sender,transactionID);
            case "tl":
                return handleTlCommand(sender,transactionID);
            case "code":
                return handleCodeCommand(sender, args,transactionID);
            case "createcode":
                return handleCreateCodeCommand(sender, args,transactionID);
            case "delete":
                return handleDeleteCommand(sender, args,transactionID);
            case "add":
                return handleAddCommand(sender, args,transactionID);
            case "reload":
                return handleReloadCommand(sender,transactionID);
            case "checktl":
                return  handleTlCommand(sender, args,transactionID);

            default:
                sender.sendMessage(lang.invalidCommand);
                return true;
        }

    }
    private boolean handleReloadCommand(CommandSender sender, String transactionID) {
        if(sender.hasPermission("betterranks.command.reload")){
            configManager.ReloadConfig();
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " BetterRanks config reloaded!");
            return true;
        }else {
            noPermissionMessage(sender,transactionID);
            return false;
        }
    }

    // Metody obsługujące różne podkomendy
    private boolean handleInfoCommand(CommandSender sender, String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleInfoCommand called", transactionID);
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " BetterRanks plugin for BetterBox.top");
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Author: " + plugin.getDescription().getAuthors());
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Version: " + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br code <code> ");
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br tl " + ChatColor.GREEN + lang.timeLeftHelpMessage);

        if(sender.isOp()){
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleInfoCommand: "+sender.getName()+" is OP "+sender.isOp());
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br delete <nick> " + ChatColor.GREEN + " - set player's rank to Player");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br add <nick> <rank> <time_amount> <s/m/d> " + ChatColor.GREEN + " - set player's rank for given time");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br createcode <quantity> <rank> <time_amount> <s/m/d> <pool_name> " + ChatColor.GREEN + " - create codes for ranks under given pool name. Each user can redeem only one code per pool.");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br reload " + ChatColor.GREEN + " - reloads config");
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " /br checktl <player>" + ChatColor.GREEN + " - return time left on player's rank.");
        }
        return true;
    }

    private boolean handleTlCommand(CommandSender sender, String transactionID) {
        if(sender.hasPermission("betterranks.command.tl")){
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleTlCommand: sender: " + sender + " sender.getName(): " + sender.getName(), transactionID);
            if (sender.hasPermission("betterranks.command.tl")) {
                UUID uuid = null;
                try {
                    uuid = Objects.requireNonNull(Bukkit.getPlayer(sender.getName())).getUniqueId();
                } catch (Exception e) {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanksCommandHandler: handleTlCommand: exception while converting username to UUID: " + e.getMessage() + " " + e, transactionID);
                }
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleTlCommand: calling getRemainingTimeFormatted with " + uuid, transactionID);
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.AQUA + plugin.dataManager.getRemainingTimeFormatted(uuid,transactionID));
            } else {
                noPermissionMessage(sender,transactionID);
            }
            return true;
        }
        noPermissionMessage(sender,transactionID);
        return false;
    }


    private boolean handleTlCommand(CommandSender sender, String[] player, String transactionID) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleTlCommand called with parameters sender: "+sender.getName()+" checkedPlayer: "+player[1], transactionID);
            if (sender.hasPermission("betterranks.command.checktl")) {
                UUID uuid = null;
                try {
                    OfflinePlayer playercheck = Bukkit.getOfflinePlayer(player[1]);
                    uuid = Objects.requireNonNull(playercheck.getUniqueId());
                } catch (Exception e) {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanksCommandHandler: handleTlCommand: exception while converting username to UUID: " + e.getMessage() + " " + e, transactionID);
                }
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleTlCommand: calling getRemainingTimeFormatted with " + uuid, transactionID);
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.AQUA + plugin.dataManager.getRemainingTimeFormatted(uuid,transactionID));
            } else {
                noPermissionMessage(sender,transactionID);
                return false;
            }
            return true;
    }

    private boolean handleCodeCommand(CommandSender sender, String[] args, String transactionID) {
        if (sender instanceof Player) {
            {
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: onCommand: /br code called", transactionID);
                try {
                    if (sender.hasPermission("betterranks.command.code")) {
                        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: onCommand: calling handleCodeUsageCommand with parameters: " + sender + " " + args[1], transactionID);
                        return handleCodeUsageCommand(sender, args[1], transactionID);
                    } else {
                        noPermissionMessage(sender,transactionID);
                    }
                } catch (Exception e) {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanksCommandHandler: onCommand: exception while checking permissions: " + e.getMessage() + " " + e, transactionID);
                }
            }
        }
        return false;
    }

    private boolean handleAddCommand(CommandSender sender, String[] args, String transactionID) {
        if(sender.hasPermission("betterranks.command.add")){
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleAddCommand called with parameters " + sender.getName() + " " + Arrays.toString(args), transactionID);
            try {
                String playerName = args[1];
                String rank = args[2];
                int amount = Integer.parseInt(args[3]);
                char timeUnit = args[4].charAt(0); // s for seconds, m for minutes, d for days
                plugin.addPlayerRank(playerName, rank, amount, timeUnit);
                sender.sendMessage("Rank added successfully for player " + playerName);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid format for amount. Usage: /br add <player> <rank> <amount> <s/m/d> " + e.getMessage(), transactionID);
                return true;
            }
        }
        noPermissionMessage(sender,transactionID);

        return false;
    }


    private boolean handleCreateCodeCommand(CommandSender sender, String[] args, String transactionID) {
        if(sender.hasPermission("betterranks.command.createcode")) {
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCreateCodeCommand called by " + sender.getName() + " " + Arrays.toString(args), transactionID);
            try {
                String maxUsers = args[1];
                String rank = args[2];
                int timeAmount = Integer.parseInt(args[3]);
                char timeUnit = args[4].charAt(0); // s for seconds, m for minutes, d for days
                poolName = args[5];

                plugin.dataManager.generateCodes(Integer.parseInt(maxUsers), rank, timeAmount, timeUnit, poolName);
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + " Code "+ChatColor.GOLD + "" + ChatColor.DARK_RED +plugin.dataManager.getCodeFromPool(poolName)+" "+ ChatColor.AQUA + " created successfully.");
                sender.sendMessage("Generated " + plugin.dataManager.getCodeFromPool(poolName)+ " code successfully.");
                pluginLogger.log(PluginLogger.LogLevel.INFO,"Code "+plugin.dataManager.getCodeFromPool(poolName)+" created successfully by "+sender.getName(), transactionID);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid number format. Usage: /br createcode <maxUsers> <rank> <amount_of_time> <time_unit> "+e.getMessage());
                pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanksCommandHandler: handleCreateCodeCommand: Invalid number format. Usage: /br createcode <maxUsers> <rank> <amount_of_time> <time_unit> "+e.getMessage(), transactionID);
                return true;
            }
        }
        noPermissionMessage(sender,transactionID);
        return false;
    }

    private void noPermissionMessage(CommandSender sender, String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: noPermissionMessage called", transactionID);
        sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.DARK_RED +lang.noPermission);
    }
    private boolean handleCodeUsageCommand(CommandSender sender, String code, String transactionID) {
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCodeUsageCommand called", transactionID);
        Player player = (Player) sender;
        if (plugin.dataManager.checkCode(code)) {

            String rankFromCode = plugin.dataManager.getCodesConfig().getString(plugin.dataManager.getPoolNameForCode(code) + ".rank");

            if (isCurrentRankHigher(player, rankFromCode)) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.DARK_RED +lang.higherRank);
                return true;
            }
            UUID playerUUID = player.getUniqueId();
            // Pobierz szczegóły kodu
            String playerName = sender.getName();
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCodeUsageCommand: playerName "+playerName+" for code "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code), transactionID);
            String rank = plugin.dataManager.getCodesConfig().getString( plugin.dataManager.getPoolNameForCode(code)+ ".rank");
            int timeAmount = plugin.dataManager.getCodesConfig().getInt(plugin.dataManager.getPoolNameForCode(code) + ".timeAmount");
            String timeUnitStr = plugin.dataManager.getCodesConfig().getString(plugin.dataManager.getPoolNameForCode(code) + ".timeUnit");
            String maxUsers = plugin.dataManager.getCodesConfig().getString(plugin.dataManager.getPoolNameForCode(code) + ".maxUsers");
            String currentUsers = plugin.dataManager.getCodesConfig().getString(plugin.dataManager.getPoolNameForCode(code) + ".currentUsers");
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler> handleCodeUsageCommand> rank: "+rank+" timeAmount: "+timeAmount+" timeUnit: "+timeUnitStr+" maxUsers: "+maxUsers+" currentUsers: "+currentUsers, transactionID);
            char timeUnit; // Domyślna wartość, jeśli konwersja zawiedzie
            //pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCodeUsageCommand: calling plugin.dataManager.checkCode(code) "+ code);
            if (timeUnitStr.length() == 1) {
                    timeUnit = timeUnitStr.charAt(0);
            } else {
                    pluginLogger.log(PluginLogger.LogLevel.ERROR,"BetterRanksCommandHandler: handleCodeUsageCommand: Invalid time unit format in config "+timeUnitStr, transactionID);
                    throw new IllegalArgumentException("Invalid time unit format");


            }


            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCodeUsageCommand: calling dataManager.canUseCode(code) "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code), transactionID);
            if(!plugin.dataManager.canUseCode(playerUUID,code)){
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks] " + ChatColor.DARK_RED +lang.poolAlreadyUsed);
                return false;
            }else{
                //int maxUsers = plugin.dataManager.getCodesConfig().getInt(plugin.dataManager.getPoolNameForCode(code) + ".maxUsers");
                pluginLogger.log(PluginLogger.LogLevel.INFO,"Code "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code)+" used successfully by "+playerName+ ". Rank " + rank + " added/extended for next " + timeAmount + timeUnitStr.charAt(0) + ".", transactionID,player.getName(),playerUUID.toString());
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA + lang.codeUsedSuccessfully+" Rank "+ ChatColor.BOLD + rank +ChatColor.BOLD+" | "+ChatColor.BOLD+ timeAmount + timeUnit +ChatColor.BOLD+ ".");
                pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: handleCodeUsageCommand: calling addPlayerRank with parameters "+playerName+","+rank+","+timeAmount+","+timeUnitStr.charAt(0), transactionID);
                plugin.addPlayerRank(playerName, rank, timeAmount, timeUnitStr.charAt(0));
                plugin.dataManager.useCode(playerUUID,code);
            }

        } else {
            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" + ChatColor.AQUA +lang.invalidOrExpiredCode);
            pluginLogger.log(PluginLogger.LogLevel.INFO,"Player "+sender+" used wrong or expired code "+code+" from pool "+plugin.dataManager.getPoolNameForCode(code), transactionID,player.getName(),player.getUniqueId().toString());

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
        pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: isCurrentRankHigher: "+player.getName()+" new rank: "+currentRankPosition+" old rank: "+newRankPosition);
        // Porównanie pozycji rang
        if (currentRankPosition > newRankPosition){
            pluginLogger.log(PluginLogger.LogLevel.DEBUG,"BetterRanksCommandHandler: isCurrentRankHigher: return true ");
            return true;
        }
        return false;
    }

    private boolean handleDeleteCommand(CommandSender sender, String[] args, String transactionID) {

        if(sender.hasPermission("betterranks.command.delete")){
            String playerName = args[1];
            UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId(); // Używaj getOfflinePlayer zamiast getPlayer
            plugin.removePlayerRank(uuid);
            pluginLogger.log(PluginLogger.LogLevel.INFO,"BetterRanksCommandHandler: onCommand: Player " + playerName + " removed", transactionID,playerName,null);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "manload");

            sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[BetterRanks]" +"Rank removed successfully for player " + playerName);
            return true;
        }
        noPermissionMessage(sender,transactionID);
        return false;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (!command.getName().equalsIgnoreCase("br")) return suggestions;

        if (args.length == 1) {
            if ("tl".startsWith(args[0].toLowerCase())) suggestions.add("tl");
            if ("code".startsWith(args[0].toLowerCase())) suggestions.add("code");
            if ("info".startsWith(args[0].toLowerCase())) suggestions.add("info");
            if ("help".startsWith(args[0].toLowerCase())) suggestions.add("help");
            if(sender.isOp()){
                if ("delete".startsWith(args[0].toLowerCase())) suggestions.add("delete");
                if ("add".startsWith(args[0].toLowerCase())) suggestions.add("add");
                if ("createcode".startsWith(args[0].toLowerCase())) suggestions.add("createcode");
                if ("reload".startsWith(args[0].toLowerCase())) suggestions.add("reload");
                if ("checktl".startsWith(args[0].toLowerCase())) suggestions.add("checktl");
            }
        }
        if (args.length == 2) {
            if ("delete".equals(args[0])) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }
            }
            if ("add".equals(args[0])) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }
            }
            if ("checktl".equals(args[0])) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }
            }
        }

        return suggestions;
    }

}
