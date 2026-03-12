package com.ajaxjs.util.httpremote;

import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * HTTP GET request implementation.
 * This class provides functionality for sending HTTP GET requests
 * to retrieve resources from a server.
 * It extends the base Request class and offers various utility methods
 * for convenient API interaction and data retrieval in different formats.
 */
@Slf4j
public class Get extends Request {
    /**
     * Creates a new Get request with the specified HTTP method and URL.
     *
     * @param method The HTTP method to use
     * @param url    The URL to send the request to
     */
    public Get(HttpMethod method, String url) {
        super(method, url);
    }

    /**
     * Creates a new Get request with the specified URL and connection initializer.
     * This constructor automatically initializes and connects to the server.
     *
     * @param url            The URL to send the GET request to
     * @param initConnection Consumer that configures the HTTP connection
     *                       before it's established
     */
    public Get(String url, Consumer<HttpURLConnection> initConnection) {
        this(HttpMethod.GET, url);

        init(initConnection);
        connect();
    }

    /**
     * Sends a GET request to the specified URL and returns the response as plain text.
     *
     * @param url The URL to send the GET request to
     * @return The response body as a String
     */
    public static String text(String url) {
        return text(url, null);
    }

    /**
     * Sends a GET request to the specified URL with custom connection initialization
     * and returns the response as plain text.
     *
     * @param url            The URL to send the GET request to
     * @param initConnection Consumer that configures the HTTP connection
     * @return The response body as a String
     */
    public static String text(String url, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().getResponseText();
    }

    /**
     * Sends a GET request to the specified URL with an authorization token.
     * Returns the response as a JSON object.
     *
     * @param url   The URL to send the GET request to
     * @param token The authorization token to include in the request header
     * @return The response parsed as a JSON map
     */
    public static Map<String, Object> api(String url, String token) {
        return api(url, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    /**
     * Sends a GET request to the specified URL with default connection settings.
     * Returns the response as a JSON object.
     *
     * @param url The URL to send the GET request to
     * @return The response parsed as a JSON map
     */
    public static Map<String, Object> api(String url) {
        return api(url, EMPTY_INIT);
    }

    /**
     * Sends a GET request to the specified URL with custom connection initialization.
     * Returns the response as a JSON object.
     *
     * @param url            The URL to send the GET request to
     * @param initConnection Consumer that configures the HTTP connection
     * @return The response parsed as a JSON map
     */
    public static Map<String, Object> api(String url, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().responseAsJson();
    }

    /**
     * Sends a GET request to the specified URL with an authorization token.
     * Returns the response mapped to the specified class type.
     *
     * @param url   The URL to send the GET request to
     * @param clz   The class to map the response to
     * @param token The authorization token to include in the request header
     * @return The response mapped to the specified class type
     */
    public static <T> T api(String url, Class<T> clz, String token) {
        return api(url, clz, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    /**
     * Sends a GET request to the specified URL with custom connection initialization.
     * Returns the response mapped to the specified class type.
     *
     * @param url            The URL to send the GET request to
     * @param clz            The class to map the response to
     * @param initConnection Consumer that configures the HTTP connection
     * @return The response mapped to the specified class type
     */
    public static <T> T api(String url, Class<T> clz, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().responseAsBean(clz);
    }

    /**
     * Sends a GET request to the specified URL with default connection settings.
     * Returns the response mapped to the specified class type.
     *
     * @param url The URL to send the GET request to
     * @param clz The class to map the response to
     * @return The response mapped to the specified class type
     */
    public static <T> T api(String url, Class<T> clz) {
        return new Get(url, EMPTY_INIT).getResp().responseAsBean(clz);
    }

    /**
     * Sends a GET request to the specified URL with custom connection initialization.
     * Returns the response as an XML map.
     *
     * @param url            The URL to send the GET request to
     * @param initConnection Consumer that configures the HTTP connection
     * @return The response parsed as an XML map
     */
    public static Map<String, String> apiXml(String url, Consumer<HttpURLConnection> initConnection) {
        return new Get(url, initConnection).getResp().responseAsXML();
    }
}