package com.ajaxjs.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random Tools - Utility class for generating various types of random values including
 * numbers, strings, and UUIDs with version 7 support.
 *
 * <p>This class provides thread-safe random generation using ThreadLocalRandom
 * for performance and SecureRandom for cryptographic-strength randomness.
 */
public class RandomTools {
    /**
     * Generate a six-digit random integer.
     * This method is a convenience wrapper for generateNumber(6).
     *
     * @return Random integer in the range 100 thousand to 999999
     */
    public static int generateNumber() {
        return generateNumber(6);
    }

    /**
     * Generate a specified-digit random integer.
     * For example, if numDigits is 3, the result will be between 100 and 999.
     *
     * @param numDigits The number of digits (must be greater than 0)
     * @return Random integer with exactly the specified number of digits
     * @throws IllegalArgumentException if numDigits is less than or equal to 0
     */
    public static int generateNumber(int numDigits) {
        if (numDigits <= 0)
            throw new IllegalArgumentException("The number of digits must be greater than zero.");

        int min = (int) Math.pow(10, numDigits - 1);
        int max = (int) Math.pow(10, numDigits) - 1;

        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Character pool containing alphanumeric characters for random string generation.
     * This includes both uppercase and lowercase letters and digits 0-9.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private static final String STR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Generate a six-character random string containing alphanumeric characters.
     * This method is a convenience wrapper for generateRandomString(6).
     *
     * @return Random string of six characters from the alphanumeric character pool
     */
    public static String generateRandomString() {
        return generateRandomString(6);
    }

    /**
     * Generate a random string of specified length containing alphanumeric characters.
     * The string consists of uppercase letters, lowercase letters, and digits.
     *
     * @param length The length of the string to be generated (must be greater than 0)
     * @return Random string containing characters from the alphanumeric character pool
     */
    public static String generateRandomString(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(STR.charAt(number));
        }

        return sb.toString();
    }

    /**
     * SecureRandom instance for cryptographic-strength random number generation.
     * Used for UUID generation where security is important.
     */
    public static final SecureRandom RANDOM = new SecureRandom();

    public static final Random SIMPLE_RANDOM = new Random();

    /**
     * Generate a UUID version 7 (UUIDv7) using timestamp-based generation.
     * UUIDv7 provides time-ordered UUIDs with better locality than random UUIDs.
     *
     * @return UUIDv7 instance
     */
    public static UUID uuid() {
        byte[] value = randomBytes();
        ByteBuffer buf = ByteBuffer.wrap(value);
        long high = buf.getLong();
        long low = buf.getLong();

        return new UUID(high, low);
    }

    /**
     * Generate a UUIDv7 string without a hyphen.
     *
     * @return UUIDv7 string without hyphen.
     */
    public static String uuidStr() {
        return uuid().toString().replace(CommonConstant.HYPHEN_STR, CommonConstant.EMPTY_STRING);
    }

    /**
     * Generate random bytes for UUIDv7 generation.
     *
     * @return Random bytes
     */
    private static byte[] randomBytes() {
        byte[] value = new byte[16]; // random bytes
        SIMPLE_RANDOM.nextBytes(value);

        ByteBuffer timestamp = ByteBuffer.allocate(Long.BYTES);// current timestamp in ms
        timestamp.putLong(System.currentTimeMillis());
        System.arraycopy(timestamp.array(), 2, value, 0, 6);// timestamp

        // version and variant
        value[6] = (byte) ((value[6] & 0x0F) | 0x70);
        value[8] = (byte) ((value[8] & 0x3F) | 0x80);

        return value;
    }

    /**
     * Show the timestamp of a UUIDv7.
     *
     * @param uuidStr UUIDv7
     * @return Date
     */
    public static Date showTime(String uuidStr) {
        long msb = UUID.fromString(uuidStr).getMostSignificantBits();
        long timestamp = (msb >>> 16) & 0xFFFFFFFFFFFFL;

        return new Date(timestamp);
    }
}