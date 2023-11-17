package betterbox.mine.game.betterranks;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BetterRanksCommandHandler implements CommandExecutor {

    private final BetterRanks plugin;

    public BetterRanksCommandHandler(BetterRanks plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("br")) {

            // Handling the 'code' command, no permission required
            if (args.length == 2 && args[0].equals("code")) {
                return handleCodeUsageCommand(sender, args[1]);
            }

            // Check permission for other /br commands
            if (!sender.hasPermission("betterranks.command.br")) {
                sender.sendMessage("You don't have permission to use this command!");
                return true;
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
                    sender.sendMessage("Invalid format for amount. Usage: /br <player> <rank> <amount> <s/m/d>");
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
        if (plugin.dataManager.useCode(code)) {
            // Pobierz szczegóły kodu
            String rank = plugin.dataManager.getCodesConfig().getString(code + ".rank");
            int timeAmount = plugin.dataManager.getCodesConfig().getInt(code + ".timeAmount");
            char timeUnit = plugin.dataManager.getCodesConfig().getString(code + ".timeUnit").charAt(0);

            // Zakładając, że 'sender' jest graczem
            String playerName = sender.getName();

            // Dodaj rangę graczowi
            plugin.addPlayerRank(playerName, rank, timeAmount, timeUnit);

            sender.sendMessage("Code used successfully. Rank " + rank + " added for " + timeAmount + timeUnit + ".");
        } else {
            sender.sendMessage("Invalid or expired code.");
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
