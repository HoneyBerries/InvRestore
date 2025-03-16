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
 * Utility class for serializing and deserializing player inventories with encryption.
 */
public class InventorySerializer {

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String HASHING_ALGORITHM = "SHA-256";
    private static final int KEY_LENGTH_BYTES = 32;
    private static final Logger LOGGER = InvRestore.getInstance().getLogger();
    private static SecretKey secretKey = null;

    /**
     * Lazy initialization getter for the secret key.
     *
     * @return the secret key used for encryption and decryption
     */
    private static SecretKey getSecretKey() {
        if (secretKey == null) {
            secretKey = generateSecretKey();
        }
        return secretKey;
    }

    /**
     * Generates a secret key based on the Minecraft version.
     *
     * @return the generated secret key
     */
    private static SecretKey generateSecretKey() {
        try {
            final String minecraftVersion = Bukkit.getMinecraftVersion();
            final MessageDigest sha = MessageDigest.getInstance(HASHING_ALGORITHM);
            final byte[] hash = sha.digest(minecraftVersion.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(hash, 0, KEY_LENGTH_BYTES, ENCRYPTION_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing algorithm " + HASHING_ALGORITHM + " not available.", e);
        }
    }

    /**
     * Serializes an array of ItemStack objects to a Base64 encoded string.
     *
     * @param items the array of ItemStack objects to serialize
     * @return the Base64 encoded string representing the serialized inventory
     */
    public static String serializeInventory(final ItemStack[] items) {
        try {
            final byte[] serializedData = serialize(items);
            final byte[] encryptedData = encrypt(serializedData);
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error serializing inventory.", e);
            return null;
        }
    }

    /**
     * Deserializes a Base64 encoded string to an array of ItemStack objects.
     *
     * @param data the Base64 encoded string representing the serialized inventory
     * @return the array of ItemStack objects
     */
    public static ItemStack[] deserializeInventory(final String data) {
        try {
            final byte[] encryptedData = Base64.getDecoder().decode(data);
            final byte[] decryptedData = decrypt(encryptedData);
            return deserialize(decryptedData);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deserializing inventory.", e);
            return null;
        }
    }

    /**
     * Serializes an array of ItemStack objects to a byte array.
     *
     * @param items the array of ItemStack objects to serialize
     * @return the byte array representing the serialized inventory
     * @throws IOException if an I/O error occurs
     */
    private static byte[] serialize(final ItemStack[] items) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream)) {
            bukkitObjectOutputStream.writeInt(items.length);
            for (final ItemStack item : items) {
                bukkitObjectOutputStream.writeObject(item);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Deserializes a byte array to an array of ItemStack objects.
     *
     * @param data the byte array representing the serialized inventory
     * @return the array of ItemStack objects
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object can't be found
     */
    private static ItemStack[] deserialize(final byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream)) {
            final int size = bukkitObjectInputStream.readInt();
            final ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) bukkitObjectInputStream.readObject();
            }
            return items;
        }
    }

    /**
     * Encrypts a byte array using the secret key.
     *
     * @param data the byte array to encrypt
     * @return the encrypted byte array
     * @throws Exception if an encryption error occurs
     */
    private static byte[] encrypt(final byte[] data) throws Exception {
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
        return cipher.doFinal(data);
    }

    /**
     * Decrypts a byte array using the secret key.
     *
     * @param data the byte array to decrypt
     * @return the decrypted byte array
     * @throws Exception if a decryption error occurs
     */
    private static byte[] decrypt(final byte[] data) throws Exception {
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
        return cipher.doFinal(data);
    }
}