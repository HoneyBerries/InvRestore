package me.honeyberries.invRestore.storage;

import me.honeyberries.invRestore.InvRestore;
import me.honeyberries.invRestore.util.InventorySerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

/**
 * Manages player inventory data storage and retrieval using a YAML file.
 */
public class PlayerInventoryData {

    private static final PlayerInventoryData INSTANCE = new PlayerInventoryData();
    private static final String INVENTORY_PATH = "inventories.";

    private final InvRestore plugin = InvRestore.getInstance();
    private File configFile;
    private YamlConfiguration yamlConfig;

    private PlayerInventoryData() {
        load();
    }

    public static PlayerInventoryData getInstance() {
        return INSTANCE;
    }

    /**
     * Loads the configuration file, creating it if necessary.
     */
    public void load() {
        configFile = new File(plugin.getDataFolder(), "inventories.yml");

        // Create file if it doesn't exist
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                plugin.getLogger().info("Created inventories.yml!");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create inventories.yml!");
                e.printStackTrace();
            }
        }

        yamlConfig = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Loaded inventories.yml!");
    }

    /**
     * Saves a player's inventory to the config file, distinguishing between death and save inventories.
     *
     * @param player            The player whose inventory is being saved.
     * @param inventory         The inventory to save.
     * @param isDeathInventory  True if it's a death inventory, false otherwise.
     */
    public void saveInventory(Player player, ItemStack[] inventory, boolean isDeathInventory) {
        if (inventory == null) return;

        String serializedInventory = InventorySerializer.serializeInventory(inventory, player);
        if (serializedInventory == null) {
            plugin.getLogger().warning("Failed to serialize inventory for " + player.getName());
            return;
        }

        if (isDeathInventory) {
            set(INVENTORY_PATH + player.getUniqueId() + ".death", serializedInventory);
        } else {
            set(INVENTORY_PATH + player.getUniqueId() + ".save", serializedInventory);
        }
    }

    /**
     * Retrieves a player's inventory from the config file, based on whether it's a death or save inventory.
     *
     * @param player           The player whose inventory is being retrieved.
     * @param isDeathInventory True if retrieving a death inventory, false for a save inventory.
     * @return The deserialized inventory or null if not found.
     */
    public ItemStack[] getSavedInventory(Player player, boolean isDeathInventory) {
        String serializedInventory = isDeathInventory
                ? yamlConfig.getString(INVENTORY_PATH + player.getUniqueId() + ".death")
                : yamlConfig.getString(INVENTORY_PATH + player.getUniqueId() + ".save");

        if (serializedInventory == null) {
            return null;
        }

        ItemStack[] inventory = InventorySerializer.deserializeInventory(serializedInventory, player);
        if (inventory == null) {
            plugin.getLogger().warning("Failed to deserialize inventory for " + player.getName());
            return null;
        }

        return inventory;
    }

    /**
     * Sets a value in the configuration and saves the updated configuration file.
     *
     * @param path  the configuration path.
     * @param value the value to set.
     */
    public void set(@NotNull String path, @Nullable Object value) {
        yamlConfig.set(path, value);
        saveConfig();
    }


    /**
     * Saves the configuration file.
     */
    private void saveConfig() {
        try {
            yamlConfig.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save inventories.yml!");
        }
    }

    /**
     * Public method to save all data.
     * This should be called when the plugin is being disabled.
     */
    public void save() {
        saveConfig();
        plugin.getLogger().info("Saved all inventory data!");
    }

}
