package me.honeyberries.invRestore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class InventorySaveCommand implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        // Check if sender has permission to use the command
        if (!sender.hasPermission("invrestore.save")) {
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
                sender.sendMessage(Component.text("Console must specify a player.").color(NamedTextColor.YELLOW));
                return true;
            }
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(Component.text("Player not found.").color(NamedTextColor.YELLOW));
                return true;
            }
        }
        PersistentDataContainer pdc = target.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(InvRestore.getInstance(), "inventory");

        ItemStack[] contents = target.getInventory().getContents();


        // Serialize inventory to Base64
        String serializedInventory = InventorySerializer.serializeInventory(contents);

        if (serializedInventory == null) {
            InvRestore.getInstance().getLogger().warning("Failed to serialize inventory for " + target.getName());
            sender.sendMessage(Component.text("Failed to save inventory for " + target.getName()).color(NamedTextColor.RED));
            return true;
        }

        // Store in PDC
        pdc.set(key, PersistentDataType.STRING, serializedInventory);

        // Send message
        sender.sendMessage(Component.text("Inventory saved for " + target.getName()).color(NamedTextColor.GREEN));

        // Play sound
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        return true;


    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {

            suggestions.add("help");
            List<String> playerList =  Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();

            suggestions.addAll(playerList);
        }
        return suggestions.stream().filter(s -> s.toLowerCase().
                startsWith(args[args.length - 1].toLowerCase())).toList();
    }



    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(Component.text("=== Inventory Save Help ===").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/invsave").color(NamedTextColor.AQUA)
                .append(Component.text(" - Save your own inventory.")));
        sender.sendMessage(Component.text("/invsave <player>").color(NamedTextColor.AQUA)
                .append(Component.text(" - Save another player's inventory.")));
        sender.sendMessage(Component.text("/invsave help").color(NamedTextColor.AQUA)
                .append(Component.text(" - Show this help message.")));
    }
}