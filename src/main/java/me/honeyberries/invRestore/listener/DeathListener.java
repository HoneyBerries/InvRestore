package me.honeyberries.invRestore.listener;

import me.honeyberries.invRestore.storage.PlayerInventoryData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Listener class for handling player death events.
 * This class is responsible for saving a player's inventory when they die.
 */
public class DeathListener implements Listener {

    private final PlayerInventoryData playerInventoryData = PlayerInventoryData.getInstance();

    /**
     * Event handler for the PlayerDeathEvent.
     * This method saves the player's inventory to the YAML file when they die.
     * Uses HIGHEST priority to ensure it runs before items are potentially dropped.
     *
     * @param event The PlayerDeathEvent.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Get a copy of the player's inventory contents before any modifications
        ItemStack[] contents = player.getInventory().getContents().clone();

        // Save the player's inventory to the YAML file
        playerInventoryData.saveInventory(player, contents, true);

        // Notify the player that their inventory has been saved
        //player.sendMessage(Component.text("Your inventory has been saved. Use /inventoryrestore death to restore it.")
                //.color(NamedTextColor.GREEN));
    }
}
