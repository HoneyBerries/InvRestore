package me.honeyberries.invRestore.storage;

import me.honeyberries.invRestore.InvRestore;
import me.honeyberries.invRestore.util.InventorySerializer;
import me.honeyberries.invRestore.util.PlayerData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles all database operations for storing and retrieving player data.
 * Uses a YAML file for persistent storage.
 */
public class PlayerDataStorage {

    /** Singleton instance of the class. */
    private static final PlayerDataStorage INSTANCE = new PlayerDataStorage();
    /** Base path in the YAML file for inventory data. */
    private static final String INVENTORY_PATH = "inventories.";

    /** Reference to the main plugin instance. */
    private InvRestore plugin;
    /** Logger instance for error reporting */
    private Logger logger;
    /** File object representing the inventories YAML file. */
    private File configFile;
    /** YAML configuration object for reading/writing inventory data. */
    private YamlConfiguration yamlConfig;
    /** Read-write lock for thread-safe access to the YAML configuration. */
    private final ReadWriteLock configLock = new ReentrantReadWriteLock();
    /** Flag to track if the database has been initialized */
    private boolean initialized = false;

    /**
     * Private constructor for a singleton pattern.
     * Initialization is deferred to the init method to avoid circular dependencies.
     */
    private PlayerDataStorage() {
        // Empty constructor - initialization happens in init()
    }

    /**
     * Initializes the database with the plugin instance.
     * This method should be called from the plugin's onEnable method.
     *
     * @param plugin The InvRestore plugin instance
     */
    public void init(InvRestore plugin) {
        if (initialized) return;

        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.configFile = new File(plugin.getDataFolder(), "inventories.yml");

        initialized = true;
    }

    /**
     * Returns the singleton instance of this class.
     *
     * @return the singleton PlayerDataStorage instance
     */
    public static PlayerDataStorage getInstance() {
        return INSTANCE;
    }

    /**
     * Loads the inventories YAML file synchronously.
     * Should only be called during plugin enable/reload.
     */
    public void loadSync() {
        configLock.writeLock().lock();
        try {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                logger.info("Created inventories.yml!");
            }
            yamlConfig = YamlConfiguration.loadConfiguration(configFile);
            logger.info("Loaded inventories.yml!");
        } catch (IOException e) {
            logger.severe("Could not create/load inventories.yml!");
            e.printStackTrace();
        } finally {
            configLock.writeLock().unlock();
        }
    }

    /**
     * Saves a player's data in a non-blocking and thread-safe manner.
     * Serialization is performed on the player's region thread, and file I/O is scheduled
     * on the global region scheduler to avoid blocking region threads.
     *
     * @param player the player whose data is being saved
     * @param isDeathInventory {@code true} if this is a death inventory, {@code false} otherwise
     */
    public void savePlayerData(Player player, boolean isDeathInventory) {
        if (player == null) return;

        player.getScheduler().run(plugin,task -> {

            // Create PlayerData directly from the player
            PlayerData playerData = PlayerData.fromPlayer(player);

            // Serialize the player data
            final String serializedData = InventorySerializer.serialize(playerData);
            if (serializedData == null) {
                logger.warning("Failed to serialize inventory for " + player.getName());
                return;
            }

            String path = INVENTORY_PATH + player.getUniqueId() + (isDeathInventory ? ".death" : ".save");

            // Schedule file write on global region scheduler (never block region thread)
            plugin.getServer().getGlobalRegionScheduler().run(plugin, (t) -> {
                configLock.writeLock().lock();
                try {
                    yamlConfig.set(path, serializedData);
                    yamlConfig.save(configFile);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Could not save inventories.yml!", e);
                } finally {
                    configLock.writeLock().unlock();
                }
            });

        }, () -> logger.warning("Failed to schedule inventory save for " + player.getName())
        );
    }

    /**
     * Retrieves a player's saved data in a thread-safe manner.
     *
     * @param player the player whose data is being retrieved
     * @param isDeathInventory {@code true} to retrieve the death inventory, {@code false} for the saved inventory
     * @return the deserialized PlayerData, or {@code null} if not found
     */
    public PlayerData getPlayerData(Player player, boolean isDeathInventory) {
        if (player == null) return null;

        String path = INVENTORY_PATH + player.getUniqueId() + (isDeathInventory ? ".death" : ".save");
        configLock.readLock().lock();
        try {
            String serializedData = yamlConfig.getString(path);
            if (serializedData == null) {
                return null;
            }
            return InventorySerializer.deserialize(serializedData);
        } finally {
            configLock.readLock().unlock();
        }
    }

    /**
     * Restores a player's inventory and XP.
     *
     * @param player the player to restore data to
     * @param isDeathInventory {@code true} to restore death inventory, {@code false} for saved inventory
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean restorePlayerData(Player player, boolean isDeathInventory) {
        PlayerData data = getPlayerData(player, isDeathInventory);
        if (data == null) {
            return false;
        }

        // Apply the data to the player
        data.applyToPlayer(player);

        logger.info("Restored inventory and XP for " + player.getName());
        return true;
    }

    /**
     * Synchronously saves all inventory data to disk.
     * Should be called when the plugin is disabled.
     */
    public void save() {
        configLock.writeLock().lock();
        try {
            yamlConfig.save(configFile);
            logger.info("Saved all inventory data!");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not save inventories.yml!", e);
        } finally {
            configLock.writeLock().unlock();
        }
    }
}
