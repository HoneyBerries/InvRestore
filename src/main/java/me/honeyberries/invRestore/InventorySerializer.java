package me.honeyberries.invRestore;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Base64;

/**
 * Utility class for serializing and deserializing inventories.
 */
public class InventorySerializer {

    /**
     * Serializes an inventory (ItemStack array) into a Base64 string.
     *
     * @param items The inventory to serialize.
     * @return The serialized Base64 string.
     */
    public static String serializeInventory(ItemStack[] items) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream)) {

            // Write inventory size
            bukkitObjectOutputStream.writeInt(items.length);

            // Write each item to stream
            for (ItemStack item : items) {
                bukkitObjectOutputStream.writeObject(item);
            }

            bukkitObjectOutputStream.close();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deserializes a Base64 string back into an inventory (ItemStack array).
     *
     * @param data The serialized Base64 inventory data.
     * @return The deserialized ItemStack array.
     */
    public static ItemStack[] deserializeInventory(String data) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream)) {

            // Read inventory size
            int size = bukkitObjectInputStream.readInt();
            ItemStack[] items = new ItemStack[size];

            // Read each item from stream
            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) bukkitObjectInputStream.readObject();
            }

            return items;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
