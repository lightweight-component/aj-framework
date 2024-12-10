package com.ajaxjs.util.spring;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.util.Base64Utils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

public class Encoder {
    /**
     * Base64-encode the given byte array to a String.
     *
     * @param src the original byte array
     * @return the encoded byte array as a UTF-8 String
     */
    public static String encodeToString(byte[] src) {
        SpelExpression expr;
        return Base64Utils.encodeToString(src);
    }

    /**
     * Base64-decode the given byte array from an UTF-8 String.
     *
     * @param src the encoded UTF-8 String
     * @return the original byte array
     */
    public static byte[] decodeFromString(String src) {
        MapAccessor d;
        return Base64Utils.decodeFromString(src);
    }

    public static String uriDecode(String src) {
        return StringUtils.uriDecode(src, StandardCharsets.UTF_8);
    }

    public static String md5(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }

}
