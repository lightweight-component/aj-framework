package com.ajaxjs.business.json.parser.util;

public class HexUtils {
    /**
     * 0 - F => 0 - 15
     */
    public static byte hexCharToNum(char c) {
        if (c >= '0' && c <= '9') return (byte) (c - '0');
        if (c >= 'a' && c <= 'f') return (byte) (c - 'a' + 10);
        if (c >= 'A' && c <= 'F') return (byte) (c - 'A' + 10);

        throw new RuntimeException("Invalid hex char");
    }

    /**
     * length 2 hex char array to byte
     */
    public static byte hexCharPairToByte(char[] s) {
        return (byte) (hexCharToNum(s[0]) * 16 + hexCharToNum(s[1]));
    }
}
