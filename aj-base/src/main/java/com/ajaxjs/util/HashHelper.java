package com.ajaxjs.util;

import com.ajaxjs.util.io.DataReader;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for cryptographic hash operations, supporting various hash algorithms
 * (MD5, SHA-1, SHA-256) and HMAC operations.
 * Provides methods for generating hashes from strings and byte arrays,
 * with options for hexadecimal and Base64 output formats.
 */
@Slf4j
@Data
@Accessors(chain = true)
public class HashHelper {
    /**
     * The name of the hash algorithm to use (e.g., "MD5", "SHA-1", "SHA-256").
     */
    private final String algorithmName;

    /**
     * The input data to hash.
     */
    private byte[] input;

    /**
     * Creates a new HashHelper instance with the specified algorithm and byte array input.
     *
     * @param algorithmName the hash algorithm name (e.g., "MD5", "SHA-1", "SHA-256", "HmacMD5")
     * @param input         the input data to hash
     */
    public HashHelper(String algorithmName, byte[] input) {
        this.algorithmName = algorithmName;
        this.input = input;
    }

    /**
     * Creates a new HashHelper instance with the specified algorithm and string input.
     * The string is converted to bytes using UTF-8 encoding.
     *
     * @param algorithmName the hash algorithm name
     * @param input         the input string to hash
     */
    public HashHelper(String algorithmName, String input) {
        this(algorithmName, new StringBytes(input).getUTF8_Bytes());
    }

    /**
     * Gets the message digest using the specified algorithm.
     *
     * @return the message digest as a byte array
     * @throws RuntimeException if the specified algorithm is not available
     */
    public byte[] getMessageDigest() {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance(algorithmName);
        } catch (NoSuchAlgorithmException e) {
            log.warn("No Such Algorithm: {}", algorithmName, e);
            throw new RuntimeException("No Such Algorithm: " + algorithmName, e);
        }

        return md.digest(input);
    }

    /**
     * The secret key used for HMAC operations.
     */
    private byte[] key;

    /**
     * Sets the secret key for HMAC operations using UTF-8 encoding.
     *
     * @param key the secret key as a string
     * @return this HashHelper instance for method chaining
     */
    public HashHelper setKey(String key) {
        this.key = new StringBytes(key).getUTF8_Bytes();
        return this;
    }

    /**
     * Sets the secret key for HMAC operations by decoding a Base64 encoded string.
     *
     * @param key the Base64 encoded secret key
     * @return this HashHelper instance for method chaining
     */
    public HashHelper setKeyBase64(String key) {
        this.key = new Base64Utils(key).decode();
        return this;
    }

    /**
     * Gets the Message Authentication Code (MAC) value using the specified algorithm.
     * If no key is set, a random key is generated.
     *
     * @return the generated MAC value as a byte array
     * @throws RuntimeException if the algorithm is not supported or the key is invalid
     */
    public byte[] getMac() {
        SecretKey sk;

        try {
            if (key == null)
                sk = KeyGenerator.getInstance(algorithmName).generateKey();
            else
                sk = new SecretKeySpec(key, algorithmName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No Such Algorithm: " + algorithmName, e);
        }

        try {
            Mac mac = Mac.getInstance(algorithmName);
            mac.init(sk);

            return mac.doFinal(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No Such Algorithm: " + algorithmName, e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("Invalid Key.", e);
        }
    }

    /**
     * Performs the hash operation based on the current configuration.
     * Uses MessageDigest if no key is set, or HMAC if a key is set.
     *
     * @return the hash value as a byte array
     */
    public byte[] hash() {
        return key == null ? getMessageDigest() : getMac();
    }

    /**
     * Performs the hash operation and returns the result as a lowercase hexadecimal string.
     *
     * @return the hash value as a lowercase hexadecimal string
     */
    public String hashAsStr() {
        return BytesHelper.bytesToHexStr(hash()).toLowerCase();
    }

    /**
     * Gets the result of the hash operation encoded in Base64 format.
     *
     * @param isWithoutPadding whether to omit the padding '=' characters
     * @return the hash value as a Base64 encoded string
     */
    public String hashAsBase64(boolean isWithoutPadding) {
        return new Base64Utils(hash()).setWithoutPadding(isWithoutPadding).encodeAsString();
    }

    /**
     * Gets the result of the hash operation encoded in Base64 format with padding.
     *
     * @return the hash value as a Base64 encoded string with padding
     */
    public String hashAsBase64() {
        return hashAsBase64(false);
    }

    /**
     * Constant for MD5 hash algorithm.
     */
    public static final String MD5 = "MD5";

    /**
     * Constant for SHA-1 hash algorithm.
     */
    public static final String SHA1 = "SHA1";

    /**
     * Constant for SHA-256 hash algorithm.
     */
    public static final String SHA256 = "SHA-256";

    /**
     * Generates an MD5 hash value for a string.
     * This is equivalent to Spring's DigestUtils.md5DigestAsHex() method.
     *
     * @param str the input string
     * @return the MD5 hash value as a lowercase hexadecimal string
     */
    public static String md5(String str) {
        return new HashHelper(MD5, str).hashAsStr();
    }

    /**
     * Generates an SHA-1 hash value for a string.
     *
     * @param str the input string
     * @return the SHA-1 hash value as a lowercase hexadecimal string
     */
    public static String getSHA1(String str) {
        return new HashHelper(SHA1, str).hashAsStr();
    }

    /**
     * Generates an SHA-256 hash value for a string.
     *
     * @param str the input string
     * @return the SHA-256 hash value as a lowercase hexadecimal string
     */
    public static String getSHA256(String str) {
        return new HashHelper(SHA256, str).hashAsStr();
    }

    /**
     * Creates a HashHelper configured for HMAC-MD5 operations with the specified input and key.
     *
     * @param str the input string
     * @param key the secret key
     * @return a HashHelper instance configured for HMAC-MD5 operations
     */
    public static HashHelper getHmacMD5(String str, String key) {
        return new HashHelper("HmacMD5", str).setKey(key);
    }

    /**
     * Constant for HMAC-SHA1 message authentication code algorithm.
     */
    public static final String HMAC_SHA1 = "HmacSHA1";

    /**
     * Constant for HMAC-SHA256 message authentication code algorithm.
     */
    public static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Generates an HMAC-SHA256 hash value for a string and returns it as a Base64 encoded string.
     *
     * @param str              the input string
     * @param key              the secret key
     * @param isWithoutPadding whether to omit padding in the Base64 output
     * @return the HMAC-SHA256 hash value as a Base64 encoded string
     */
    public static String getHmacSHA256(String str, String key, boolean isWithoutPadding) {
        return new HashHelper(HMAC_SHA256, str).setKey(key).hashAsBase64(isWithoutPadding);
    }

    /**
     * Calculates the MD5 hash of a file from an input stream.
     * The file is processed in chunks to handle large files efficiently.
     *
     * @param in the input stream containing the file data
     * @return the MD5 hash value as a lowercase hexadecimal string
     * @throws RuntimeException if MD5 algorithm is not available
     */
    public static String calcFileMD5(InputStream in) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            new DataReader(in).readStreamAsBytes(8192, (readSize, buffer) -> digest.update(buffer, 0, readSize));

            return BytesHelper.bytesToHexStr(digest.digest()).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            log.warn("No Such Algorithm: MD5", e);
            throw new RuntimeException("No Such Algorithm: MD5", e);
        }
    }

    /**
     * Calculates the MD5 hash of a byte array.
     *
     * @param bytes the byte array containing the file data
     * @return the MD5 hash value as a lowercase hexadecimal string
     */
    public static String calcFileMD5(byte[] bytes) {
        return calcFileMD5(new ByteArrayInputStream(bytes));
    }
}