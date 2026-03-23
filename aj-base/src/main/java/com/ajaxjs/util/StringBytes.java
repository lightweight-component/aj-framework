package com.ajaxjs.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Convert String to bytes and bytes to String
 * Actually, you can use `new String(bytes)`/`str.getBytes()` more easily.
 */
public class StringBytes {
    /**
     * String to convert.
     */
    private String string;

    /**
     * Constructor.
     *
     * @param string String to convert.
     */
    public StringBytes(String string) {
        this.string = string;
    }

    /**
     * Bytes to convert.
     */
    private byte[] bytes;

    /**
     * Constructor.
     *
     * @param bytes Bytes to convert.
     */
    public StringBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * Convert String to bytes.
     *
     * @param charset Charset to use.
     * @return Bytes
     */
    public byte[] getBytes(Charset charset) {
        return charset == null ? string.getBytes() : string.getBytes(charset);
    }

    /**
     * Convert String to bytes.
     * Uses platform default charset.
     *
     * @return Bytes
     */
    public byte[] getBytes() {
        return getBytes(null);
    }

    /**
     * Convert String to bytes.
     * Uses UTF-8 charset.
     *
     * @return Bytes
     */
    public byte[] getUTF8_Bytes() {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Convert bytes to String.
     *
     * @param charset Charset to use.
     * @return String with given charset.
     */
    public String getString(Charset charset) {
        return charset == null ? new String(bytes) : new String(bytes, charset);
    }

    /**
     * Convert bytes to String.
     * Uses platform default charset.
     *
     * @return String with platform default charset.
     */
    public String getString() {
        return getString(null);
    }

    /**
     * Convert bytes to String.
     * Uses UTF-8 charset.
     *
     * @return String with UTF-8 charset.
     */
    public String getUTF8_String() {
        return getString(StandardCharsets.UTF_8);
    }

    /**
     * Show platform default charset.
     *
     * @return Platform default charset.
     */
    public static String showPlatformDefaultCharset() {
        return Charset.defaultCharset().toString();
    }
}