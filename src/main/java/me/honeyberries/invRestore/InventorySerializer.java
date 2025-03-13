package me.honeyberries.invRestore;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for serializing and deserializing inventories with AES-256 encryption.
 */
public class InventorySerializer {

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String HASHING_ALGORITHM = "SHA-256";
    private static final int KEY_LENGTH_BYTES = 32; // 256 bits
    private static final Logger LOGGER = InvRestore.getInstance().getLogger(); // Use plugin logger
    private static final SecretKey SECRET_KEY = generateSecretKey();

    /**
     * Generates the AES-256 encryption key based on the Minecraft version.
     *
     * @return The generated SecretKey.
     * @throws IllegalStateException If the hashing algorithm is not available.
     */
    private static SecretKey generateSecretKey() {
        try {
            // Create an SHA-256 hash of the Minecraft version to use as the AES key
            final String minecraftVersion = Bukkit.getMinecraftVersion();
            final MessageDigest sha = MessageDigest.getInstance(HASHING_ALGORITHM);
            final byte[] hash = sha.digest(minecraftVersion.getBytes(StandardCharsets.UTF_8));

            // Return the first 32 bytes as the AES-256 key
            return new SecretKeySpec(hash, 0, KEY_LENGTH_BYTES, ENCRYPTION_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing algorithm " + HASHING_ALGORITHM + " not available.", e);
        }
    }

    /**
     * Serializes an inventory (ItemStack array) into a Base64 string and encrypts it using AES-256.
     *
     * @param items The inventory to serialize.
     * @return The encrypted, serialized Base64 string, or null if an error occurs.
     */
    public static String serializeInventory(final ItemStack[] items) {
        try {
            // Serialize the inventory
            final byte[] serializedData = serialize(items);

            // Encrypt the serialized data
            final byte[] encryptedData = encrypt(serializedData);

            // Convert encrypted data to Base64 and return
            return Base64.getEncoder().encodeToString(encryptedData);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error serializing inventory.", e);
            return null;
        }
    }

    /**
     * Deserializes an encrypted Base64 string back into an inventory (ItemStack array) using AES-256.
     *
     * @param data The encrypted Base64 inventory data.
     * @return The deserialized ItemStack array, or null if an error occurs.
     */
    public static ItemStack[] deserializeInventory(final String data) {
        try {
            // Decode Base64 and decrypt the data
            final byte[] encryptedData = Base64.getDecoder().decode(data);
            final byte[] decryptedData = decrypt(encryptedData);

            // Deserialize the data back into ItemStacks
            return deserialize(decryptedData);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deserializing inventory.", e);
            return null;
        }
    }

    /**
     * Serializes an ItemStack array into a byte array.
     *
     * @param items The ItemStack array to serialize.
     * @return The serialized byte array.
     * @throws IOException If an I/O error occurs during serialization.
     */
    private static byte[] serialize(final ItemStack[] items) throws IOException {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             final BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream)) {

            // Write inventory size
            bukkitObjectOutputStream.writeInt(items.length);

            // Write each item to stream
            for (final ItemStack item : items) {
                bukkitObjectOutputStream.writeObject(item);
            }

            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Deserializes a byte array into an ItemStack array.
     *
     * @param data The byte array to deserialize.
     * @return The deserialized ItemStack array.
     * @throws IOException            If an I/O error occurs during deserialization.
     * @throws ClassNotFoundException If a class of a serialized object cannot be found.
     */
    private static ItemStack[] deserialize(final byte[] data) throws IOException, ClassNotFoundException {
        try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             final BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream)) {

            // Read inventory size
            final int size = bukkitObjectInputStream.readInt();
            final ItemStack[] items = new ItemStack[size];

            // Read each item from stream
            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) bukkitObjectInputStream.readObject();
            }

            return items;
        }
    }

    /**
     * Encrypts data using AES-256.
     *
     * @param data The data to encrypt.
     * @return The encrypted data.
     * @throws Exception If an error occurs during encryption.
     */
    private static byte[] encrypt(final byte[] data) throws Exception {
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, InventorySerializer.SECRET_KEY);
        return cipher.doFinal(data);
    }

    /**
     * Decrypts data using AES-256.
     *
     * @param data The data to decrypt.
     * @return The decrypted data.
     * @throws Exception If an error occurs during decryption.
     */
    private static byte[] decrypt(final byte[] data) throws Exception {
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, InventorySerializer.SECRET_KEY);
        return cipher.doFinal(data);
    }
}