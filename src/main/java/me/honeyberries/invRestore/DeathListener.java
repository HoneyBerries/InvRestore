package me.honeyberries.invRestore;

import org.bukkit.event.EventHandler;
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
     *
     * @param event The PlayerDeathEvent.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Get player's inventory contents
        ItemStack[] contents = player.getInventory().getContents();

        // Save the player's inventory to the YAML file
        playerInventoryData.saveInventory(player, contents, true);
    }
}
