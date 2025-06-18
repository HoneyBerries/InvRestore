package me.honeyberries.invRestore;

import me.honeyberries.invRestore.command.InventorySaveCommand;
import me.honeyberries.invRestore.command.InventoryViewCommand;
import me.honeyberries.invRestore.command.RestoreCommand;
import me.honeyberries.invRestore.listener.DeathListener;
import me.honeyberries.invRestore.listener.GUIListener;
import me.honeyberries.invRestore.storage.PlayerDataStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * The main plugin class for InvRestore.
 * This plugin allows server administrators and players to save, restore, and view inventories.
 * Inventories can be saved on player death or manually with commands.
 */
public final class InvRestore extends JavaPlugin {

    /**
     * Metadata key used to track when a player has a restore inventory GUI open.
     * This prevents conflicts with other inventory interactions.
     */
    public static final String RESTORE_INVENTORY_METADATA = "restoreInventoryOpen";

    /**
     * Called when the plugin is enabled.
     * Initializes the database, registers event listeners, and sets up commands.
     */
    @Override
    public void onEnable() {
        getLogger().info("InvRestore has been enabled!");

        // Initialize and load the player data storage system
        PlayerDataStorage.getInstance().init(this);
        PlayerDataStorage.getInstance().loadSync();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        // Register commands with their command executors
        Objects.requireNonNull(getCommand("inventoryrestore")).setExecutor(new RestoreCommand());
        Objects.requireNonNull(getCommand("inventorysave")).setExecutor(new InventorySaveCommand());
        Objects.requireNonNull(getCommand("inventorysaveview")).setExecutor(new InventoryViewCommand());
    }

    /**
     * Called when the plugin is disabled.
     * Ensures all player data is saved to disk before shutdown.
     */
    @Override
    public void onDisable() {
        // Save all pending player data to disk
        PlayerDataStorage.getInstance().save();

        getLogger().info("InvRestore has been disabled!");
    }

    /**
     * Gets the singleton instance of the InvRestore plugin.
     * Provides static access to the plugin from other classes.
     *
     * @return The InvRestore plugin instance.
     */
    public static InvRestore getInstance() {
        return getPlugin(InvRestore.class);
    }
}
