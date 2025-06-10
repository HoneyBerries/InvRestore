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
 * This plugin allows players to save, view, and restore their inventories.
 */
public final class InvRestore extends JavaPlugin {

    public final String RESTORE_INVENTORY_METADATA = "restoreInventoryOpen";


    /**
     * Called when the plugin is enabled.
     * Registers listeners and commands.
     */
    @Override
    public void onEnable() {
        getLogger().info("InvRestore has been enabled!");

        // Initialize and load the database
        PlayerDataStorage.getInstance().init(this);
        PlayerDataStorage.getInstance().loadSync();

        // Register events
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        // Register commands with their correct base names from plugin.yml
        Objects.requireNonNull(getCommand("inventoryrestore")).setExecutor(new RestoreCommand());
        Objects.requireNonNull(getCommand("inventorysave")).setExecutor(new InventorySaveCommand());
        Objects.requireNonNull(getCommand("inventorysaveview")).setExecutor(new InventoryViewCommand());
    }

    /**
     * Called when the plugin is disabled.
     * Ensures all data is saved before shutdown.
     */
    @Override
    public void onDisable() {
        // Save any pending data
        PlayerDataStorage.getInstance().save();

        getLogger().info("InvRestore has been disabled!");
    }


    /**
     * Gets the instance of the InvRestore plugin.
     *
     * @return The InvRestore plugin instance.
     */
    public static InvRestore getInstance() {
        return getPlugin(InvRestore.class);
    }

}

