package me.honeyberries.invRestore.command;

import me.honeyberries.invRestore.InvRestore;
import me.honeyberries.invRestore.storage.PlayerDataStorage;
import me.honeyberries.invRestore.util.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * Command executor for viewing a player's saved inventory.
 */
public class InventoryViewCommand implements TabExecutor {

    private final InvRestore plugin = InvRestore.getInstance();
    private final PlayerDataStorage database = PlayerDataStorage.getInstance();
    private static final String RESTORE_INVENTORY_METADATA = InvRestore.getInstance().RESTORE_INVENTORY_METADATA;
    private static final String VIEW_PERMISSION = "invrestore.view";

    /**
     * Handles the execution of the command.
     *
     * @param sender  The sender of the command.
     * @param command The command that was executed.
     * @param label   The alias of the command used.
     * @param args    The arguments passed to the command.
     * @return true if the command was handled successfully, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission(VIEW_PERMISSION)) {
            sender.sendMessage(Component.text("You do not have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(Component.text("This command can only be used by players.")
                    .color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))
                || args.length > 2) {
            sendHelpMessage(sender);
            return true;
        }

        final String inventoryType = args[0].toLowerCase();
        final Player target = getTargetPlayer(playerSender, args);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
            return true;
        }

        final ItemStack[] savedInventory = getSavedInventory(target, inventoryType);

        if (savedInventory == null) {
            sender.sendMessage(Component.text("No saved inventory found for " + target.getName() +
                    " with the specified type.").color(NamedTextColor.YELLOW));
            return true;
        }

        final Inventory gui = createInventoryGUI(target, savedInventory);

        // Open the inventory GUI for the player and set metadata to prevent item movement
        playerSender.openInventory(gui);
        playerSender.setMetadata(RESTORE_INVENTORY_METADATA, new FixedMetadataValue(plugin, true));

        return true;
    }

    /**
     * Retrieves the target player based on the command arguments.
     *
     * @param self The player executing the command.
     * @param args The command arguments.
     * @return The target player, or the player executing the command if no target is specified.
     */
    private Player getTargetPlayer(Player self, String[] args) {
        return args.length == 2 ? Bukkit.getPlayer(args[1]) : self;
    }

    /**
     * Retrieves the saved inventory for the specified player and type.
     *
     * @param target The target player.
     * @param type   The type of inventory to retrieve (death or save).
     * @return The saved inventory, or null if no inventory is found.
     */
    private ItemStack[] getSavedInventory(Player target, String type) {
        boolean isDeathInventory = "death".equals(type);
        if (isDeathInventory || "save".equals(type)) {
            PlayerData playerData = database.getPlayerData(target, isDeathInventory);
            if (playerData != null) {
                return playerData.getInventoryContents();
            }
        }
        return null;
    }

    /**
     * Creates an inventory GUI for the specified player and inventory data.
     *
     * @param target        The target player.
     * @param inventoryData The inventory data to display.
     * @return The created inventory GUI.
     */
    private Inventory createInventoryGUI(Player target, ItemStack[] inventoryData) {
        final Inventory gui = Bukkit.createInventory(null, 54,
                Component.text(target.getName()).color(NamedTextColor.DARK_GREEN).append(Component.text("'s Inventory").color(NamedTextColor.GOLD)));

        // Set the inventory items in the GUI
        for (int i = 0; i < inventoryData.length; i++) {
            if (inventoryData[i] != null) {
                gui.setItem(i, inventoryData[i]);
            }
        }

        // Note: The armor items are already included in the inventoryData array
        // No need to set them separately from the current player's inventory
        return gui;
    }

    /**
     * Sends a help message to the command sender.
     *
     * @param sender The sender of the command.
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("--- Inventory View Help ---").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/inventorysaveview <death | save> <player>")
                .color(NamedTextColor.AQUA)
                .append(Component.text(" - View a player's last death or saved inventory.")));
        sender.sendMessage(Component.text("/inventorysaveview help")
                .color(NamedTextColor.AQUA)
                .append(Component.text(" - Show this help message.")));
    }

    /**
     * Handles tab completion for the command.
     *
     * @param sender The sender of the command.
     * @param command The command that was executed.
     * @param alias The alias of the command used.
     * @param args The arguments passed to the command.
     * @return A list of possible completions for the final argument, or an empty list if no completions are available.
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("death", "save", "help")
                    .filter(option -> option.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        } else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
