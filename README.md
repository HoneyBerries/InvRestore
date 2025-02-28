# **InvRestore**

InvRestore is a Minecraft plugin that allows players to restore their inventory upon death. It saves a player's inventory when they die, and the player can restore it using the /restore command.

This plugin is perfect for preventing players from losing their valuable items when they die, making it useful for both survival and creative servers.


## **Features**

Automatic Inventory Saving: When a player dies, their inventory is saved automatically.

Restore Command: Players can restore their saved inventory using the `/restore` command.

Permission-Based Access: Only players with the invrestore.restore permission can use the `/restore` command.

Tab Completion: Supports tab completion for player names and help command.


## **Installation**

Download the Plugin:
Download the latest version of InvRestore.jar from the Releases section.

Place the Plugin in Your Server:

Drop the InvRestore.jar file into your server's plugins folder.

Restart the Server:
After placing the plugin in the plugins folder, restart your server to enable the plugin.



## **Commands**

`/restore`

Restores the player's inventory. If the command is used by a player, it restores that player's inventory. If used by a console, the player must be specified.

`/restore <player>`

Restores the inventory of another player by specifying their name.

`/restore help`

Displays the help message for the /restore command.

## **Permissions**

`invrestore.restore`

This permission allows players to use the `/restore` command.


## **Configuration**

No configuration is required for basic functionality, but if you want to modify the plugin in the future, a configuration file will be added for more customization options.


## **How It Works**

When a player dies, their inventory is serialized and stored in their PersistentDataContainer.
The inventory is stored using Base64 encoding, making it easy to save and restore.
Players can restore their inventory at any time by using the `/restore` command.


## **Troubleshooting**

Error: `java.io.NotSerializableException`

If you encounter this error, it typically means that a non-serializable object (such as a custom item) is being stored in the player's inventory. Ensure that all items in the inventory are serializable, or modify the plugin to handle those specific cases.

Error: Player not found

If you see this error when using `/restore <player>`, it means the specified player is either offline or their inventory has not been saved.


## **Contributing**

We welcome contributions to improve InvRestore. If you have ideas for new features, bug fixes, or improvements, please open an issue or submit a pull request.

Steps for contributing:
Fork the repository.
Create a new branch for your feature/bug fix.
Commit your changes.
Push your changes and submit a pull request.


## **License**

InvRestore is licensed under the MIT License. See the LICENSE file for details.
