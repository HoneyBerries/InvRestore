# InvRestore

A simple Bukkit/Spigot plugin that allows players to restore inventories saved at death or pre-saved with `/invsave`.

## Features

- Automatically saves player inventory upon death.
- Allows restoring the last death or manually saved inventory.
- Offers inventory inspection with a GUI.
- Config-based storage system using `inventories.yml`.

## Commands

- **/invsave** [player]: Saves the current inventory for the sender or the specified player.
- **/restore** \<death|save\> [player]: Restores death or saved inventory for yourself or another player.
- **/invview** \<death|save\> [player]: Opens a GUI to view death or saved inventory for yourself or another player.

## Permissions

- **invrestore.save**: Allows using `/invsave`.
- **invrestore.restore**: Allows using `/restore`.
- **invrestore.view**: Allows using `/invview`.

## Installation

1. Place the plugin .jar file in your server's `plugins` folder.
2. Start or restart your server.
3. Edit the generated `inventories.yml` if necessary.
4. Adjust permissions as desired in your permission management system.

## Support and Contributing

- **Issues**: Report bugs and feature requests under the GitHub repository's issues section.
- **Pull Requests**: Contributions are welcome. Please fork this repository and submit a pull request.