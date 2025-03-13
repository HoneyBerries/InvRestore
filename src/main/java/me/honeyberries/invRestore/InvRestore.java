package me.honeyberries.invRestore;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * The main plugin class for InvRestore.
 * This plugin allows players to restore their inventory upon death using the /restore command.
 */
public final class InvRestore extends JavaPlugin {

    /**
     * Called when the plugin is enabled.
     * Registers the DeathListener and the RestoreCommand.
     */
    @Override
    public void onEnable() {
        getLogger().info("InvRestore has been enabled!");

        // Load the plugin configuration
        PlayerInventoryData.getInstance().load();

        // Register events (pass plugin instance)
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        // Register commands
        Objects.requireNonNull(getCommand("restore")).setExecutor(new RestoreCommand());
        Objects.requireNonNull(getCommand("invsave")).setExecutor(new InventorySaveCommand());
        Objects.requireNonNull(getCommand("invview")).setExecutor(new InventoryViewCommand());
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
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
