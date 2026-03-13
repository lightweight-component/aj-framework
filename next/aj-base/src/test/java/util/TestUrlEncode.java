package util;

import com.ajaxjs.util.UrlEncode;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestUrlEncode {
    @Test
    void testEncode_NormalCase() {
        assertEquals("Hello+World%21+%E4%BD%A0%E5%A5%BD", new UrlEncode("Hello World! 你好").encode());
    }

    // 测试 encodeQuery()
    @Test
    void testEncodeQuery_ReplacesSpacesWithPercent20() {
        assertEquals("hello%20world", new UrlEncode("hello world").encodeQuery());
    }

    // 测试 encodeSafe()
    @Test
    void testEncodeSafe_ReplacesSpecialCharacters() {
        assertEquals("a%20b%2A%2Ac%7Ed%2Fe", new UrlEncode("a+b*c~d/e").encodeSafe());
    }

    // 测试 decode()
    @Test
    void testDecode_NormalCase() {
        assertEquals("Hello World! 你好", new UrlEncode("Hello+World%21+%E4%BD%A0%E5%A5%BD").decode());
    }

    // 测试 urlChinese()
    @Test
    void testUrlChinese_ConvertsISOToUTF8() {
        String isoStr = new String("你好".getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        assertEquals("你好", UrlEncode.urlChinese(isoStr));
    }

    @Test
    public void testParseStringToMap() {
        Map<String, String> expected = new HashMap<>();
        expected.put("bar", "baz");
        assertEquals(expected, UrlEncode.parseStringToMap("foo&bar=baz"));
    }
}
