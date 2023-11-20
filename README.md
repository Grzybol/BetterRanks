BetterRanks

Overview
BetterRanks is a versatile plugin for Bukkit-based Minecraft servers, offering a dynamic ranking system. It allows server administrators to manage player ranks effectively, using various commands and integrating a unique code system for rank promotions.

Features
Rank Management: Easily assign, extend, or remove ranks for players.
Code System: Generate and use unique codes for rank promotions.
Time Tracking: Check the remaining time of a player's rank.
Admin Commands: Special commands for server administrators to manage ranks and codes.
Commands
The plugin introduces several commands under the br main command:

/br info: Displays plugin information and usage instructions.
/br tl: Returns the remaining time on your rank.
/br code <code>: Use a promotional code.
/br createcode <quantity> <rank> <time_amount> <s/m/d> <pool_name>: Generate codes for ranks under a specified pool name.
/br delete <nick>: Remove a player's rank.
/br add <nick> <rank> <time_amount> <s/m/d>: Set a player's rank for a given time.
Permissions
betterranks.command.tl
betterranks.command.code
betterranks.command.createcode
betterranks.command.delete
betterranks.command.add
Installation
Download the BetterRanks plugin jar file.
Place it into your server's plugins directory.
Restart your server to load the plugin.
Configuration
database.yml: Stores player data and used pools.
codes.yml: Contains generated codes and their corresponding pool information.
Usage
After installing and configuring BetterRanks, you can use the above commands to manage player ranks and codes. The DataManager class handles the backend operations like code generation, rank assignment, and time tracking.

API
BetterRanks provides an API for developers to interact with its features programmatically. You can access player ranks, manipulate them, or integrate the ranking system into other plugins.

Support
If you encounter any issues or have questions, please open an issue on the GitHub repository.

Contribution
Contributions are welcome! Feel free to fork the repository and submit pull requests.