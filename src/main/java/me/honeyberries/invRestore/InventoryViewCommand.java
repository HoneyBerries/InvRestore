package me.honeyberries.invRestore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command handler for the /invview command.
 * Allows players to view the saved inventory of another player in a GUI.
 */
public class InventoryViewCommand implements TabExecutor {

    // Instance of the main plugin class to access plugin methods and data
    private final InvRestore plugin = InvRestore.getInstance();

    // Instance of PlayerInventoryData to access inventory data
    private final PlayerInventoryData playerInventoryData = PlayerInventoryData.getInstance();

    private static final String VIEW_PERMISSION = "invrestore.view";

    /**
     * Executes the /invview command.
     *
     * @param sender  The command sender (player or console).
     * @param command The command executed.
     * @param label   The alias used.
     * @param args    The command arguments.
     * @return True if the command executed successfully, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Check if sender has permission to use the command
        if (!sender.hasPermission(VIEW_PERMISSION)) {
            sender.sendMessage(Component.text("You do not have permission to use this command").color(NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player playerSender)) {
            sender.sendMessage(Component.text("This command can only be used by players.").color(NamedTextColor.RED));
            return true;
        }

        // Ensure there are exactly two arguments (inventory type and player, or just inventory type)
        if (args.length < 1 || args.length > 2) {
            sendHelpMessage(sender);
            return true;
        }

        // Get the inventory type (either "death" or "save")
        String inventoryType = args[0].toLowerCase();


        // Determine the target player
        Player target;
        if (args.length == 2) {
            // If a second argument is provided, it must be the player
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
                return true;
            }

        } else {
            // If no player is specified, use the sender's inventory
            target = (Player) sender;
        }

        // Retrieve the player's inventory data based on the type (death or saved)

        ItemStack[] inventory = null;

        // If the inventory type is "death", get the death inventory
        if (inventoryType.equals("death")) {
            inventory = playerInventoryData.getSavedInventory(target, true);

        // If the inventory type is "save", get the saved inventory
        } else if (inventoryType.equals("save")) {
            inventory = playerInventoryData.getSavedInventory(target, false);
        }

        // If the inventory type is invalid, show help message
        else {
            sendHelpMessage(sender);
            return true;
        }


        // If inventory is null, no inventory was found
        if (inventory == null) {
            sender.sendMessage(Component.text("No saved inventory found for " + target.getName() + " with the specified type.")
                    .color(NamedTextColor.YELLOW));
            return true;
        }


        // Create a new inventory with 54 slots for the player
        Inventory gui = Bukkit.createInventory(null, 54, Component.text(target.getName() + "'s Inventory").color(NamedTextColor.GREEN));

        // Add items to the inventory (including armor slots)
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                gui.setItem(i, inventory[i]);
            }
        }

        // Add armor items to the top row (slots 36 to 39 in the GUI)
        if (target.getInventory().getHelmet() != null) {
            gui.setItem(36, target.getInventory().getHelmet());
        }
        if (target.getInventory().getChestplate() != null) {
            gui.setItem(37, target.getInventory().getChestplate());
        }
        if (target.getInventory().getLeggings() != null) {
            gui.setItem(38, target.getInventory().getLeggings());
        }
        if (target.getInventory().getBoots() != null) {
            gui.setItem(39, target.getInventory().getBoots());
        }

        // Open the inventory GUI for the sender
        playerSender.openInventory(gui);

        return true;
    }

    /**
     * Sends a help message to the sender.
     *
     * @param sender The command sender.
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("--- Inventory View Help ---").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/invview <death | save> <player>").color(NamedTextColor.AQUA)
                .append(Component.text(" - View a player's last death or saved inventory.")));
        sender.sendMessage(Component.text("/invview help").color(NamedTextColor.AQUA)
                .append(Component.text(" - Show this help message.")));
    }

    /**
     * Handles tab completion for the /invview command.
     *
     * @param sender  The command sender (player or console).
     * @param command The command being executed.
     * @param alias   The alias used for the command.
     * @param args    The arguments passed to the command.
     * @return A list of possible tab completions.
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> options = Arrays.asList("death", "save", "help");

            completions = options.stream()
                    .filter(option -> option != null && option.toLowerCase().startsWith(args[0].toLowerCase())) // Ensure option is not null
                    .toList();

        } else if (args.length == 2) {
            completions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase())) // Ensure name is not null
                    .toList();
        }

        return completions;
    }

}
