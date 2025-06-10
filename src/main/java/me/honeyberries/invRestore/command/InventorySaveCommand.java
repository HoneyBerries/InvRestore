package me.honeyberries.invRestore.command;

import me.honeyberries.invRestore.InvRestore;
import me.honeyberries.invRestore.storage.PlayerDataStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the /invsave command, which allows players to save their inventory
 * into the YAML file for later restoration.
 */
public class InventorySaveCommand implements TabExecutor {

    // Instance of the main plugin class to access plugin methods and data
    private final InvRestore plugin = InvRestore.getInstance();

    // Instance of PlayerDataStorage to access inventory data
    private final PlayerDataStorage database = PlayerDataStorage.getInstance();

    private static final String SAVE_PERMISSION = "invrestore.save";

    /**
     * Executes the /invsave command.
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
        if (!sender.hasPermission(SAVE_PERMISSION)) {
            sender.sendMessage(Component.text("You do not have permission to use this command.").color(NamedTextColor.RED));
            return true;
        }

        // Show help message if "help" is entered or too many arguments are given
        if (args.length > 1 || (args.length == 1 && args[0].equalsIgnoreCase("help"))) {
            sendHelpMessage(sender);
            return true;
        }

        Player target;

        // If no arguments, assume sender wants to save their own inventory
        if (args.length == 0) {
            if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage(Component.text("Console must specify a player.").color(NamedTextColor.YELLOW));
                return true;
            }
        } else {
            // If one argument is provided, attempt to find the specified player
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.RED));
                return true;
            }
        }

        // Save the player's inventory to the YAML file (not death inventory)
        database.savePlayerData(target, false);

        // Notify the sender that the inventory was saved successfully
        sender.sendMessage(Component.text("Inventory saved for " + target.getName()).color(NamedTextColor.GREEN));

        if (sender != target) {
            String senderName = (sender instanceof Player) ? sender.getName() : "Console";
            target.sendMessage(Component.text("Inventory saved by " + senderName).color(NamedTextColor.GREEN));
        }

        // Play a success sound for the target player
        if (sender instanceof Player player)
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        return true;
    }

    /**
     * Provides tab completion for the /invsave command.
     *
     * @param sender The command sender.
     * @param command The command executed.
     * @param alias The alias used.
     * @param args The command arguments.
     * @return A list of suggested completions.
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("help");

            // Add all online players' names to the suggestions list
            List<String> playerList = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();
            suggestions.addAll(playerList);
        }

        // Filter suggestions to match input
        return suggestions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .toList();
    }

    /**
     * Sends a help message to the command sender.
     *
     * @param sender The command sender.
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("--- Inventory Save Help ---").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/inventorysave").color(NamedTextColor.AQUA)
                .append(Component.text(" - Save your own inventory.")));
        sender.sendMessage(Component.text("/inventorysave <player>").color(NamedTextColor.AQUA)
                .append(Component.text(" - Save another player's inventory.")));
        sender.sendMessage(Component.text("/inventorysave help").color(NamedTextColor.AQUA)
                .append(Component.text(" - Show this help message.")));
    }
}
