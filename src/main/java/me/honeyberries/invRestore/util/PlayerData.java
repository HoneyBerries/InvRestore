package me.honeyberries.invRestore.util;

import me.honeyberries.invRestore.InvRestore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a container for storing and restoring player inventory and experience data.
 * This class is designed to be serialized and deserialized for persistent storage.
 */
public class PlayerData implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Plugin instance
    private static final InvRestore plugin = InvRestore.getInstance();

    // Logger instance for error reporting
    private static final Logger logger = plugin.getLogger();

    // Serialized inventory contents stored as a Base64 string
    private String serializedInventory;

    // Player's total experience points
    private final int totalXp;
    // Player's experience level
    private final int expLevel;
    // Player's progress towards the next level (0.0 to 1.0)
    private final float expProgress;

    /**
     * Constructs a new PlayerData object with the specified inventory and experience data.
     *
     * @param inventory   The player's inventory
     * @param totalXp     The player's total experience points
     * @param expLevel    The player's experience level
     * @param expProgress The player's progress towards the next level (0.0 to 1.0)
     */
    public PlayerData(Inventory inventory, int totalXp, int expLevel, float expProgress) {
        // Transient because Bukkit inventory is not serializable
        this.totalXp = totalXp;
        this.expLevel = expLevel;
        this.expProgress = expProgress;

        if (inventory != null) {
            // Serialize the inventory contents to a Base64 string
            this.serializedInventory = serializeInventoryContents(inventory.getContents());
        }
    }

    /**
     * Creates a PlayerData object from a Player instance.
     *
     * @param player The player to extract data from
     * @return A new PlayerData object containing the player's inventory and experience data
     */
    public static PlayerData fromPlayer(Player player) {
        if (player == null) return null;
        return new PlayerData(
            player.getInventory(),
            player.getTotalExperience(),
            player.getLevel(),
            player.getExp()
        );
    }

    /**
     * Serializes an array of ItemStacks into a Base64 encoded string.
     *
     * @param items The ItemStack array to serialize
     * @return A Base64 encoded string representing the serialized items
     */
    private String serializeInventoryContents(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the array length
            dataOutput.writeInt(items.length);

            // Serialize each item in the array
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            // Cleanup
            dataOutput.close();

            // Encode to Base64 and return
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not serialize item stacks", e);
            return null;
        }
    }

    /**
     * Deserializes the Base64 encoded inventory string back into an ItemStack array.
     *
     * @return The deserialized ItemStack array, or an empty array if deserialization fails
     */
    public ItemStack[] getInventoryContents() {
        if (serializedInventory == null) {
            return new ItemStack[0];
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(serializedInventory));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            // Read the array length
            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];

            // Deserialize each ItemStack from the stream
            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not deserialize item stacks", e);
            return new ItemStack[0];
        }
    }

    /**
     * Gets the player's total experience points.
     *
     * @return The total experience points
     */
    public int getTotalXp() {
        return totalXp;
    }

    /**
     * Gets the player's experience level.
     *
     * @return The experience level
     */
    public int getExpLevel() {
        return expLevel;
    }

    /**
     * Gets the player's progress towards the next experience level.
     *
     * @return The progress as a float (0.0 to 1.0)
     */
    public float getExpProgress() {
        return expProgress;
    }

    /**
     * Applies the stored inventory and experience data to a Player instance.
     *
     * @param player The player to apply the data to
     */
    public void applyToPlayer(Player player) {
        if (player == null) return;

        // Apply inventory
        ItemStack[] contents = getInventoryContents();
        if (contents != null) {
            player.getInventory().clear();
            player.getInventory().setContents(contents);
        }

        // Apply experience
        player.setTotalExperience(0); // Reset XP to avoid odd behavior
        player.setLevel(0);
        player.setExp(0);
        player.giveExp(totalXp);
        player.setLevel(expLevel);
        player.setExp(expProgress);
    }
}