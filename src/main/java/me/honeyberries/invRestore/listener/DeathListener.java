package me.honeyberries.invRestore.listener;

import me.honeyberries.invRestore.storage.PlayerDataStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Listener class for handling player death events.
 * This class is responsible for saving a player's inventory when they die.
 */
public class DeathListener implements Listener {

    private final PlayerDataStorage database = PlayerDataStorage.getInstance();

    /**
     * Event handler for the PlayerDeathEvent.
     * This method saves the player's inventory to the YAML file when they die.
     * Uses HIGHEST priority to ensure it runs before items are potentially dropped.
     *
     * @param event The PlayerDeathEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Save the player's inventory and XP
        database.savePlayerData(player, true);

        // Notify the player that their inventory has been saved
        player.sendMessage(Component.text("Your inventory has been saved.")
                .color(NamedTextColor.AQUA));
    }
}
