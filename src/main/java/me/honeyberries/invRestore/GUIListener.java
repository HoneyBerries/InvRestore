package me.honeyberries.invRestore;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * Listener to handle custom GUI interactions for restricted inventories.
 */
public class GUIListener implements Listener {

    private static final String RESTORE_INVENTORY_METADATA = "restoreInventoryOpen";
    private final InvRestore plugin = InvRestore.getInstance();

    /**
     * Handles InventoryClickEvent to prevent moving items from the restricted inventory.
     *
     * @param event The InventoryClickEvent to process.
     */
    @EventHandler
    public void onClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        // If there is no clicked inventory, nothing needs to be done.
        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getInventory())) {
            return;
        }
        // If the player doesn't have the metadata, ignore the event.
        if (!player.hasMetadata(RESTORE_INVENTORY_METADATA)) {
            return;
        }

        final Inventory restrictedInventory = event.getInventory();
        // If the clicked inventory is the restricted one, cancel the event.
        if (event.getClickedInventory().equals(restrictedInventory)) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles InventoryCloseEvent to remove metadata when the restricted inventory is closed.
     *
     * @param event The InventoryCloseEvent to process.
     */
    @EventHandler
    public void onClose(final InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (player.hasMetadata(RESTORE_INVENTORY_METADATA)) {
            player.removeMetadata(RESTORE_INVENTORY_METADATA, plugin);
        }
    }
}