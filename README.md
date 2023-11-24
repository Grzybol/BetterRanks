BetterRanks Plugin for Minecraft - Spigot 1.18.2+

!!!REQUIRES GroupManager to work!!!
Overview
BetterRanks is a versatile Minecraft plugin designed to enhance server management, especially in the realm of user ranks and permissions. It offers robust features for rank management, automated rank expiry, and a sophisticated command handling system.

Features
Rank Management: Dynamically manage user ranks with time-based expiry.
Command Handling: Includes a comprehensive command handler for user interactions.
Configurable: Fully customizable settings via a configuration manager.
Logging: Integrated logging system for tracking plugin activities.
Promo Codes: Generate and manage promo codes for user ranks.
Installation
Download the BetterRanks plugin .jar file.
Place it into your Minecraft server's plugins directory.
Restart the server, or load the plugin dynamically if your server supports it.
Configuration

Permissions
BetterRanks plugin uses a set of permissions to control access to various commands and functionalities. Here is the list of permissions along with their descriptions:

betterranks.info: Allows access to the /br info command. Provides general information about the plugin.
- betterranks.add: Grants permission to use the /br add command. This command is used to assign or update a player's rank.
- betterranks.delete: Grants permission to use the /br delete command. This command is used to remove a rank from a player.
- betterranks.reload: Allows the use of /br reload to reload the plugin's configuration.
- betterranks.code: Grants access to use promo codes via the /br code command.
- betterranks.createcode: Allows the creation of promo codes through the /br createcode command.


The plugin is highly configurable through the config.yml file. You can set logging levels and groups hierarchy.

Sample Configuration:

###################################
log_level:
  - INFO
  - WARNING
  - ERROR
  - DEBUG
  - DEBUG_LVL2
###################################
groups:
  1: Player
  2: VIP
  3: MVP
  4: PRO
  5: GOD
  6: Helper
  7: Owner

# Additional configurations
Usage
Commands
- /br info: Displays plugin information.
- /br add [player] [rank] [time] [unit]: Adds or updates a rank for a player.
- /br delete [player]: Removes a rank from a player.
- /br code [code]: Redeem your code, each user can redeem code from given pool only once!
- /br createcode [maxUsers] [rank] [time] [unit] [poolName]: creates a random code under custom pool name. 
- /br reload: Reloads config
Rank Expiry Check
The plugin automatically checks for rank expiries at configurable intervals and updates accordingly.

Classes Description
BetterRanks: The main class for initializing and managing the plugin.
BetterRanksCommandHandler: Handles all the commands issued by the players.
ConfigManager: Manages the plugin's configurations.
PluginLogger: Takes care of logging all the operations and errors.
DataManager: Manages player data, including rank information and promo codes.
