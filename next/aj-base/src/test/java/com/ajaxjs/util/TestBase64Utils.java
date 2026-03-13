package com.ajaxjs.util;

import com.ajaxjs.util.Base64Utils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestBase64Utils {
    @Test
    void testBase64() {
        String result = new Base64Utils("abc").encodeAsString();
        assertEquals("YWJj", result);

        result = new Base64Utils("中国人").encodeAsString();
        assertEquals("5Lit5Zu95Lq6", result);

        assertEquals("中国人", new Base64Utils(new Base64Utils("中国人").encodeAsString()).decodeAsString());
    }

    @Test
    void testEncode() {
        String urlEncoded = new Base64Utils("hello world!").setUrlSafe(true).encodeAsString();
        assertEquals("aGVsbG8gd29ybGQh", urlEncoded);
    }

    @Test
    void testDecode() {
        String text = new Base64Utils("aGVsbG8gd29ybGQh").decodeAsString();
        assertEquals("hello world!", text);
    }

    static final String HELLO_WORLD = "hello world!";

    @Test
    void testCharset() {
        Base64Utils base64Utils = new Base64Utils(HELLO_WORLD);
        String utf8 = base64Utils.encodeAsString();
        String jdk = Base64.getEncoder().encodeToString(HELLO_WORLD.getBytes(StandardCharsets.UTF_8));

        assertEquals(utf8, jdk);
    }
}
