package me.honeyberries.invRestore.util;

import me.honeyberries.invRestore.InvRestore;

import java.io.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class for serializing and deserializing player data.
 * Handles all compression and encoding/decoding operations.
 */
public class InventorySerializer {

    private static final Logger LOGGER = InvRestore.getInstance().getLogger();

    /**
     * Serializes a PlayerData object to a Base64 string.
     *
     * @param data The PlayerData object to serialize
     * @return Base64 encoded string, or null if serialization fails
     */
    public static String serialize(PlayerData data) {
        if (data == null) {
            LOGGER.warning("Cannot serialize null PlayerData");
            return null;
        }

        try {
            // Serialize the PlayerData object
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream out = new ObjectOutputStream(bos)) {
                out.writeObject(data);
                byte[] bytes = bos.toByteArray();

                // Compress the byte array to reduce size
                byte[] compressedBytes = compress(bytes);

                // Encode the byte array to a Base64 string
                return Base64.getEncoder().encodeToString(compressedBytes);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize PlayerData", e);
            return null;
        }
    }

    /**
     * Deserializes a Base64 string to a PlayerData object.
     *
     * @param data Base64 encoded string to deserialize
     * @return The deserialized PlayerData object, or null if deserialization fails
     */
    public static PlayerData deserialize(String data) {
        if (data == null || data.isEmpty()) {
            LOGGER.warning("Cannot deserialize null or empty string");
            return null;
        }

        try {
            // Decode the Base64 string to a byte array
            byte[] compressedBytes = Base64.getDecoder().decode(data);

            // Decompress the byte array
            byte[] bytes = decompress(compressedBytes);

            // Deserialize the byte array back into a PlayerData object
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                 ObjectInputStream in = new ObjectInputStream(bis)) {
                return (PlayerData) in.readObject();
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize PlayerData", e);
            return null;
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Class not found during deserialization", e);
            return null;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Invalid Base64 encoding", e);
            return null;
        }
    }

    /**
     * Compresses a byte array using GZIP.
     *
     * @param data The byte array to compress
     * @return Compressed byte array
     * @throws IOException If compression fails
     */
    private static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
        }
        return bos.toByteArray();
    }

    /**
     * Decompresses a GZIP compressed byte array.
     *
     * @param data The compressed byte array
     * @return Decompressed byte array
     * @throws IOException If decompression fails
     */
    private static byte[] decompress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(data))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) > 0) {
                bos.write(buffer, 0, len);
            }
        }
        return bos.toByteArray();
    }
}
