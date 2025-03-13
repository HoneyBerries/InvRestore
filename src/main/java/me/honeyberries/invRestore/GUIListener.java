package me.honeyberries.invRestore;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;


public class GUIListener implements Listener {

    private final InvRestore plugin = InvRestore.getInstance();

    /**
     * Handles the InventoryClickEvent to prevent players from stealing items
     * from the restricted inventory while still allowing them to interact with
     * their own inventory.
     *
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Check if it's the restricted inventory and the player has metadata for it
        if (player.hasMetadata("restoreInventoryOpen") && event.getClickedInventory() != null) {
            Inventory restrictedInventory = event.getInventory();

            // If the clicked inventory is the restricted inventory, prevent stealing
            if (event.getClickedInventory().equals(restrictedInventory)) {
                event.setCancelled(true); // Prevent moving items from the restricted inventory
            }
            // Allow interaction with the player's inventory

        }
    }

    /**
     * Handles the InventoryCloseEvent to clean up metadata when the inventory is closed.
     *
     * @param event The InventoryCloseEvent.
     */
    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (player.hasMetadata("restoreInventoryOpen")) {
            player.removeMetadata("restoreInventoryOpen", plugin);
        }
    }
}
