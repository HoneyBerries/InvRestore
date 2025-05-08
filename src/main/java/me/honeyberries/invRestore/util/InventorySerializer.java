package me.honeyberries.invRestore.util;

import me.honeyberries.invRestore.InvRestore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Nullable;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for securely serializing and deserializing player inventories.
 * Uses AES encryption for confidentiality and HMAC for integrity.
 */
public class InventorySerializer {

    // Encryption and HMAC configuration constants
    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int KEY_LENGTH_BYTES = 32; // 256 bits
    private static final int IV_LENGTH_BYTES = 16;  // 128 bits (AES block size)
    private static final int HMAC_LENGTH_BYTES = 32; // 256 bits (SHA-256 output)
    private static final int PBKDF2_ITERATIONS = 100000; // Key derivation iterations
    private static final Logger LOGGER = InvRestore.getInstance().getLogger();

    /**
     * Derives a cryptographic key from the player's UUID and a purpose string using PBKDF2.
     * This ensures separate keys for encryption and HMAC.
     *
     * @param uuid    Player's UUID (used as password)
     * @param purpose Purpose string ("enc" or "mac", used as salt)
     * @return SecretKey for the specified purpose
     * @throws Exception if key derivation fails
     */
    private static SecretKey deriveKey(UUID uuid, String purpose) throws Exception {
        char[] password = uuid.toString().toCharArray();
        byte[] salt = ("invRestore" + purpose).getBytes(StandardCharsets.UTF_8);
        KeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BYTES * 8);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    /**
     * Serializes and encrypts a player's inventory, then appends an HMAC for integrity.
     *
     * @param items  Array of ItemStack to serialize
     * @param player Player whose inventory is being serialized
     * @return Base64-encoded string of encrypted and authenticated inventory, or null on error
     */
    public static String serializeInventory(ItemStack[] items, Player player) {
        UUID uuid = player.getUniqueId();
        try {
            // Serialize inventory to bytes
            byte[] serializedData = serialize(items);
            // Encrypt serialized data
            byte[] encryptedData = encrypt(serializedData, uuid);
            // Generate HMAC for encrypted data
            byte[] hmac = generateHMAC(encryptedData, uuid);
            // Combine encrypted data and HMAC
            byte[] combinedData = combineArrays(encryptedData, hmac);
            // Encode to Base64 for storage/transmission
            return Base64.getEncoder().encodeToString(combinedData);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error serializing inventory.", e);
            return null;
        }
    }

    /**
     * Decrypts and deserializes a player's inventory from a Base64-encoded string.
     * Verifies HMAC before decryption to ensure data integrity.
     *
     * @param data   Base64-encoded string of encrypted and authenticated inventory
     * @param player Player whose inventory is being deserialized
     * @return Array of ItemStack, or null if verification or deserialization fails
     */
    public static @Nullable ItemStack[] deserializeInventory(String data, Player player) {
        UUID uuid = player.getUniqueId();
        try {
            // Decode Base64 to get combined encrypted data and HMAC
            byte[] combinedData = Base64.getDecoder().decode(data);
            // Extract encrypted data and HMAC
            byte[] encryptedData = extractEncryptedData(combinedData);
            byte[] hmac = extractHMAC(combinedData);
            // Verify HMAC before decryption
            if (!verifyHMAC(encryptedData, hmac, uuid)) {
                throw new SecurityException("HMAC verification failed. Data may have been tampered with.");
            }
            // Decrypt data
            byte[] decryptedData = decrypt(encryptedData, uuid);
            // Deserialize to ItemStack array
            return deserialize(decryptedData);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deserializing inventory.", e);
            return null;
        }
    }

    /**
     * Serializes an array of ItemStack objects to a byte array using BukkitObjectOutputStream.
     *
     * @param items Array of ItemStack to serialize
     * @return Byte array of serialized inventory
     * @throws IOException if serialization fails
     */
    private static byte[] serialize(ItemStack[] items) throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream objectStream = new BukkitObjectOutputStream(byteStream)) {
            objectStream.writeInt(items.length);
            for (ItemStack item : items) {
                objectStream.writeObject(item);
            }
            return byteStream.toByteArray();
        }
    }

    /**
     * Deserializes a byte array to an array of ItemStack objects using BukkitObjectInputStream.
     *
     * @param data Byte array to deserialize
     * @return Array of ItemStack
     * @throws IOException            if deserialization fails
     * @throws ClassNotFoundException if ItemStack class is not found
     */
    private static ItemStack[] deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             BukkitObjectInputStream objectStream = new BukkitObjectInputStream(byteStream)) {
            int size = objectStream.readInt();
            ItemStack[] items = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) objectStream.readObject();
            }
            return items;
        }
    }

    /**
     * Encrypts data using AES/CBC/PKCS5Padding with a random IV and a key derived from the player's UUID.
     * The IV is prepended to the encrypted data.
     *
     * @param data Data to encrypt
     * @param uuid Player's UUID for key derivation
     * @return Byte array containing IV + encrypted data
     * @throws Exception if encryption fails
     */
    private static byte[] encrypt(byte[] data, UUID uuid) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        byte[] iv = new byte[IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv); // Generate random IV
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKey encryptionKey = deriveKey(uuid, "enc");
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, ivSpec);
        byte[] encryptedData = cipher.doFinal(data);
        // Prepend IV to encrypted data
        byte[] combined = new byte[IV_LENGTH_BYTES + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, IV_LENGTH_BYTES);
        System.arraycopy(encryptedData, 0, combined, IV_LENGTH_BYTES, encryptedData.length);
        return combined;
    }

    /**
     * Decrypts data using AES/CBC/PKCS5Padding with a key derived from the player's UUID.
     * Expects the IV to be prepended to the encrypted data.
     *
     * @param data Byte array containing IV + encrypted data
     * @param uuid Player's UUID for key derivation
     * @return Decrypted byte array
     * @throws Exception if decryption fails
     */
    private static byte[] decrypt(byte[] data, UUID uuid) throws Exception {
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        byte[] iv = new byte[IV_LENGTH_BYTES];
        System.arraycopy(data, 0, iv, 0, IV_LENGTH_BYTES); // Extract IV
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        byte[] encryptedData = new byte[data.length - IV_LENGTH_BYTES];
        System.arraycopy(data, IV_LENGTH_BYTES, encryptedData, 0, encryptedData.length);
        SecretKey encryptionKey = deriveKey(uuid, "enc");
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, ivSpec);
        return cipher.doFinal(encryptedData);
    }

    /**
     * Generates an HMAC for the given data using a key derived from the player's UUID.
     *
     * @param data Data to authenticate
     * @param uuid Player's UUID for key derivation
     * @return HMAC as a byte array
     * @throws Exception if HMAC generation fails
     */
    private static byte[] generateHMAC(byte[] data, UUID uuid) throws Exception {
        SecretKey macKey = deriveKey(uuid, "mac");
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(macKey);
        return mac.doFinal(data);
    }

    /**
     * Verifies the HMAC for the given data using a key derived from the player's UUID.
     *
     * @param data Data to verify
     * @param hmac Expected HMAC
     * @param uuid Player's UUID for key derivation
     * @return true if HMAC matches, false otherwise
     * @throws Exception if HMAC verification fails
     */
    private static boolean verifyHMAC(byte[] data, byte[] hmac, UUID uuid) throws Exception {
        byte[] generatedHMAC = generateHMAC(data, uuid);
        return MessageDigest.isEqual(generatedHMAC, hmac);
    }

    /**
     * Combines encrypted data and HMAC into a single byte array.
     * Encrypted data comes first, followed by the HMAC.
     *
     * @param encryptedData Encrypted data
     * @param hmac          HMAC
     * @return Combined byte array
     */
    private static byte[] combineArrays(byte[] encryptedData, byte[] hmac) {
        byte[] combinedData = new byte[encryptedData.length + hmac.length];
        System.arraycopy(encryptedData, 0, combinedData, 0, encryptedData.length);
        System.arraycopy(hmac, 0, combinedData, encryptedData.length, hmac.length);
        return combinedData;
    }

    /**
     * Extracts the encrypted data from a combined array (excluding the HMAC).
     *
     * @param combinedData Combined array of encrypted data + HMAC
     * @return Encrypted data
     */
    private static byte[] extractEncryptedData(byte[] combinedData) {
        byte[] encryptedData = new byte[combinedData.length - HMAC_LENGTH_BYTES];
        System.arraycopy(combinedData, 0, encryptedData, 0, encryptedData.length);
        return encryptedData;
    }

    /**
     * Extracts the HMAC from a combined array (last HMAC_LENGTH_BYTES bytes).
     *
     * @param combinedData Combined array of encrypted data + HMAC
     * @return HMAC as a byte array
     */
    private static byte[] extractHMAC(byte[] combinedData) {
        byte[] hmac = new byte[HMAC_LENGTH_BYTES];
        System.arraycopy(combinedData, combinedData.length - HMAC_LENGTH_BYTES, hmac, 0, hmac.length);
        return hmac;
    }
}