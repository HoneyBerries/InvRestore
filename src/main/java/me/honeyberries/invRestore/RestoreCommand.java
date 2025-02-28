package me.honeyberries.invRestore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Command executor for the /restore command.
 * Allows players to restore their last saved inventory upon death.
 */
public class RestoreCommand implements TabExecutor {

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
        if (!sender.hasPermission("invrestore.restore")) {
            sender.sendMessage(Component.text("You do not have permission to use this command")
                    .color(NamedTextColor.RED));
            return true;
        }

        // Show help message if "help" is entered or too many arguments are given
        if (args.length > 1 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sendHelpMessage(sender);
            return true;
        }

        Player target;

        if (args.length == 0) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(Component.text("Console must specify a player.").color(NamedTextColor.RED));
                return true;
            }
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
                return true;
            }
        }

        PersistentDataContainer pdc = target.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(InvRestore.getInstance(), "inventory");
        if (pdc.has(key, PersistentDataType.STRING)) {
            String serializedInventory = pdc.get(key, PersistentDataType.STRING);
            ItemStack[] inventory = InventorySerializer.deserializeInventory(serializedInventory);

            if (inventory != null) {
                target.getInventory().setContents(inventory);
                sender.sendMessage(Component.text("Inventory restored for " + target.getName()).color(NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Failed to restore inventory.").color(NamedTextColor.RED));
            }
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
        sender.sendMessage(Component.text("=== Inventory Restore Help ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/restore").color(NamedTextColor.AQUA)
                .append(Component.text(" - Restore your own inventory.")));
        sender.sendMessage(Component.text("/restore <player>").color(NamedTextColor.AQUA)
                .append(Component.text(" - Restore another player's inventory.")));
        sender.sendMessage(Component.text("/restore help").color(NamedTextColor.AQUA)
                .append(Component.text(" - Show this help message.")));
    }

    /**
     * Handles tab completion for the /restore command.
     * Suggests player names or "help" if appropriate.
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

        // If the sender has permission, suggest player names or "help"
        if (args.length == 1) {
            if (sender.hasPermission("invrestore.restore")) {
                if ("help".startsWith(args[0].toLowerCase())) {
                    suggestions.add("help");
                }
                // Suggest player names
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        suggestions.add(player.getName());
                    }
                }
            }
        }

        return suggestions;
    }
}
