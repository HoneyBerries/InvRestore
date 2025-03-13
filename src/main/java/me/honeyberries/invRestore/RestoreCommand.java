package me.honeyberries.invRestore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Command executor for the /restore command.
 * Allows players to restore their last saved inventory upon death.
 */
public class RestoreCommand implements TabExecutor {

    // Instance of the main plugin class to access plugin methods and data
    private final InvRestore plugin = InvRestore.getInstance();

    // Instance of PlayerInventoryData to access inventory data
    private final PlayerInventoryData playerInventoryData = PlayerInventoryData.getInstance();

    private static final String RESTORE_PERMISSION = "invrestore.restore";



    /**
     * Executes the /restore command.
     *
     * @param sender  The command sender (player or console).
     * @param command The command being executed.
     * @param label   The command label (e.g., "restore").
     * @param args    The command arguments.
     * @return True if the command was executed successfully, false otherwise.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // Check if sender has permission to use the command
        if (!sender.hasPermission(RESTORE_PERMISSION)) {
            sender.sendMessage(Component.text("You do not have permission to use this command.")
                    .color(NamedTextColor.RED));
            return true;
        }

        Player target;

        // Determine the target player based on arguments
        if (args.length == 1 && sender instanceof Player player) {
            target = player;

        } else if (args.length == 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
                return true;
            }
        } else {
            sendHelpMessage(sender);
            return true;
        }

        // Determine which inventory to restore (death or save)
        String inventoryType = getInventoryType(args[0]);
        if (inventoryType.equals("invalid")) {
            sendHelpMessage(sender);
            return true;
        }

        boolean isDeathInventory = inventoryType.equals("death");

        // Attempt to retrieve and restore inventory
        return restoreInventory(sender, target, isDeathInventory);
    }

    /**
     * Retrieves the appropriate inventory type based on the argument.
     *
     * @param arg The argument passed by the user.
     * @return "death" if it's a death inventory, "save" if it's a save inventory, or "invalid" if the argument is invalid.
     */
    private @NotNull String getInventoryType(@NotNull String arg) {
        String lowerCaseArg = arg.toLowerCase();
        if (lowerCaseArg.equals("death")) {
            return "death";
        } else if (lowerCaseArg.equals("save")) {
            return "save";
        } else {
            return "invalid";
        }
    }

    /**
     * Attempts to restore the player's inventory using the PlayerInventoryData.
     *
     * @param sender           The command sender.
     * @param target           The player whose inventory is being restored.
     * @param isDeathInventory True if restoring a death inventory, false for a save inventory.
     * @return True if the inventory was successfully restored, false otherwise.
     */
    private boolean restoreInventory(CommandSender sender, Player target, boolean isDeathInventory) {

        // Attempt to retrieve the inventory from PlayerInventoryData
        ItemStack[] inventory = playerInventoryData.getSavedInventory(target, isDeathInventory);

        // Check if inventory was found
        if (inventory != null) {
            target.getInventory().setContents(inventory);
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            sender.sendMessage(Component.text("Inventory successfully restored for " + target.getName())
                    .color(NamedTextColor.GREEN));

            // Notify the target player if the sender is not the target
            if (sender != target) {
                target.sendMessage(Component.text("Your inventory has been restored by " + sender.getName())
                        .color(NamedTextColor.GREEN));
            }

        //Failure to restore inventory (shouldn't be called if inventory is not null)
        } else {
            sender.sendMessage(Component.text("No saved inventory found.").color(NamedTextColor.YELLOW));
        }

        return true;
    }

    /**
     * Sends a help message to the sender.
     *
     * @param sender The command sender.
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("---- Inventory Restore Help ----").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/restore death").color(NamedTextColor.AQUA)
                .append(Component.text(" - Restore your last inventory before death.")));
        sender.sendMessage(Component.text("/restore save").color(NamedTextColor.AQUA)
                .append(Component.text(" - Restore your last manually saved inventory.")));
        sender.sendMessage(Component.text("/restore <death|save> <player>").color(NamedTextColor.AQUA)
                .append(Component.text(" - Restore a player's inventory. ")));
    }

    /**
     * Handles tab completion for the /restore command.
     * Suggests "death", "save", or player names.
     *
     * @param sender  The command sender (player or console).
     * @param command The command being executed.
     * @param alias   The alias used for the command.
     * @param args    The arguments passed to the command.
     * @return A list of possible tab completions.
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("death");
            suggestions.add("save");
        } else if (args.length == 2) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                suggestions.add(player.getName());
            }
        }

        return suggestions.stream().filter(option -> option.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
    }
}
