package com.ajaxjs.util;

import lombok.Data;
import lombok.experimental.Accessors;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * Utility class for Base64 encoding and decoding operations.
 * Provides convenient methods for converting between byte arrays and strings
 * using standard Base64 encoding, with support for URL-safe variant and
 * customizable padding options.
 */
@Data
@Accessors(chain = true)
public class Base64Utils {
    /**
     * The input byte array to encode or decode.
     */
    private byte[] input;

    /**
     * Creates a new Base64Utils instance with byte array input.
     *
     * @param input the byte array to encode or decode
     */
    public Base64Utils(byte[] input) {
        this.input = input;
    }

    /**
     * Creates a new Base64Utils instance with string input using UTF-8 charset.
     *
     * @param input the string to encode or decode
     */
    public Base64Utils(String input) {
        this(input, StandardCharsets.UTF_8);
    }

    /**
     * Creates a new Base64Utils instance with string input using specified charset.
     *
     * @param input   the string to encode or decode
     * @param charset the charset to use for string to byte conversion
     */
    public Base64Utils(String input, Charset charset) {
        this.input = input.getBytes(charset);
    }

    /**
     * Flag indicating whether to omit padding characters in the encoded output.
     * This option is only relevant for encoding operations.
     * There two advantages if it's to be true: shorter for strings and without URL encoding for the character '='.
     * For example, in the use of JWT, it's not necessary to use padding characters.
     */
    private boolean withoutPadding = false;

    /**
     * Flag indicating whether to use URL-safe Base64 encoding and decoding.
     * URL-safe variant uses '-' instead of '+' and '_' instead of '/'.
     */
    private boolean urlSafe = false;

    /**
     * Encode the input then returns the result in bytes
     *
     * @return The result in bytes.
     */
    public byte[] encode() {
        if (input == null)
            throw new IllegalStateException("Input is not set");

        Encoder encoder;

        if (urlSafe)
            encoder = withoutPadding ? Base64.getUrlEncoder().withoutPadding() : Base64.getUrlEncoder();
        else
            encoder = withoutPadding ? Base64.getEncoder().withoutPadding() : Base64.getEncoder();

        return encoder.encode(input);
    }

    /**
     * Decode the input in BASE64 then returns the string.
     * Charset ISO_8859_1 is enough for a BASE64 result.
     *
     * @return The result in string.
     */
    public String encodeAsString() {
        return new String(encode(), StandardCharsets.ISO_8859_1);
    }

    /**
     * Decode the input then returns the result in bytes
     *
     * @return The result in bytes.
     */
    public byte[] decode() {
        if (input == null)
            throw new IllegalStateException("Input is not set");

        Decoder decoder = urlSafe ? Base64.getUrlDecoder() : Base64.getDecoder();

        return decoder.decode(input);
    }

    /**
     * Decode the input in BASE64 then returns the string with specified charset
     *
     * @param charset you can specify the charset of the result
     * @return The result in string.
     */
    public String decodeAsString(Charset charset) {
        return new String(decode(), charset);
    }

    /**
     * Decode the input in BASE64 then returns the string in UTF-8 charset
     *
     * @return The result in string.
     */
    public String decodeAsString() {
        return new String(decode(), StandardCharsets.UTF_8);
    }

    /**
     * Formats a Base64 string by inserting a newline character every 64 characters.
     * This follows the traditional Base64 format standard which recommends line breaks
     * after every 64 characters for readability.
     *
     * @param base64 the Base64 string to format
     * @return the formatted Base64 string with line breaks
     */
    public static String formatBase64String(String base64) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < base64.length(); i += 64)
            sb.append(base64, i, Math.min(i + 64, base64.length())).append("\n");

        return sb.toString().trim();
    }
}