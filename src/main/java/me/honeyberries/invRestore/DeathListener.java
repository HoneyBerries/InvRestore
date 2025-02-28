package me.honeyberries.invRestore;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;

/**
 * Listener class for handling player death events.
 * This class is responsible for saving a player's inventory when they die.
 */
public class DeathListener implements Listener {

    /**
     * Event handler for the PlayerDeathEvent.
     * This method serializes the player's inventory and stores it in their persistent data container.
     *
     * @param event The PlayerDeathEvent.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(InvRestore.getInstance(), "inventory");

        // Get player's inventory contents
        ItemStack[] contents = player.getInventory().getContents();


        // Serialize inventory to Base64
        String serializedInventory = InventorySerializer.serializeInventory(contents);
        if (serializedInventory == null) {
            InvRestore.getInstance().getLogger().warning("Failed to serialize inventory for " + player.getName());
            return;
        }

        // Store in PDC
        pdc.set(key, PersistentDataType.STRING, serializedInventory);
    }

}
