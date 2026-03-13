package com.ajaxjs.util;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * URL Encoding and Decoding Utility Class
 * <p>
 * This class provides methods for encoding and decoding URLs, handling Chinese characters in URLs,
 * and parsing query strings into maps.
 * It supports different encoding strategies and charset specifications.
 */
@Slf4j
@Data
@Accessors(chain = true)
public class UrlEncode {
    /**
     * The string input
     */
    private String input;

    /**
     * The charset of the input string
     */
    private Charset charset;

    /**
     * Create a new UrlEncode with input string.
     * It's a default constructor.
     *
     * @param input The string input
     */
    public UrlEncode(String input) {
        this(input, StandardCharsets.UTF_8);
    }

    /**
     * Create a new UrlEncode with input string and specified charset
     *
     * @param input   The string input
     * @param charset The charset of the input string
     */
    public UrlEncode(String input, Charset charset) {
        this.input = input;
        this.charset = charset;
    }

    /**
     * Encode the url with specified charset
     *
     * @return The string being URL encoded
     */
    public String encode() {
        try {
            return URLEncoder.encode(input, charset.toString());
        } catch (UnsupportedEncodingException e) {
            log.warn("Unsupported URL Encoding.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Encode the url, best for GET request
     * <p>
     * <a href="https://www.cnblogs.com/del88/p/6496825.html">...</a>
     *
     * @return The string being URL encoded
     */
    public String encodeQuery() {
        return encode().replace("+", "%20");
    }

    /**
     * Do the url encode
     * TODO ToFixMe: fails in UnitTest
     *
     * @return The string being URL encoded
     */
    public String encodeSafe() {
        String encoded = encode();

        return encoded.replace("+", "%20").replace("*", "%2A").replace("~", "%7E").replace("/", "%2F");
    }

    /**
     * Decode the url.
     *
     * @return The string being URL decode.
     */
    public String decode() {
        try {
            return URLDecoder.decode(input, charset.toString());
        } catch (UnsupportedEncodingException e) {
            log.warn("Unsupported URL Encoding.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * URL 网址的中文乱码处理。 如果 Tomcat 过滤器设置了 UTF-8 那么这里就不用重复转码了
     *
     * @param str 通常是 URL 的 Query String 参数
     * @return 中文
     */
    public static String urlChinese(String str) {
        return new StringBytes(str.getBytes(StandardCharsets.ISO_8859_1)).getUTF8_String();
    }

    /**
     * 字符串转 Map，格式为 {@code xxx=xxx&xxx=xxx}
     *
     * @param accessTokenStr 待转换的字符串
     * @return map
     */
    public static Map<String, String> parseStringToMap(String accessTokenStr) {
        Map<String, String> res;

        if (accessTokenStr.contains("&")) {
            String[] fields = accessTokenStr.split("&");
            res = new HashMap<>(ObjectHelper.getInitialCapacity(fields.length));

            for (String field : fields) {
                if (field.contains("=")) {
                    String[] keyValue = field.split("=");
                    String key = new UrlEncode(keyValue[0]).decode();
                    String value = keyValue.length == 2 ? new UrlEncode(keyValue[1]).decode() : null;

                    res.put(key, value);
                }
            }
        } else
            res = new HashMap<>(0);

        return res;
    }
}