package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * HTTP DELETE request implementation.
 * This class provides functionality for sending HTTP DELETE requests
 * to remove resources from a server.
 * It extends the base Request class and offers various utility methods
 * for convenient API interaction.
 */
public class Delete extends Request {
    /**
     * Creates a new Delete request with the specified HTTP method and URL.
     *
     * @param method The HTTP method to use
     * @param url    The URL to send the request to
     */
    public Delete(HttpMethod method, String url) {
        super(method, url);
    }

    /**
     * Creates a new Delete request with the specified URL and connection initializer.
     * This constructor automatically initializes and connects to the server.
     *
     * @param url            The URL to send the DELETE request to
     * @param initConnection Consumer that configures the HTTP connection
     *                       before it's established
     */
    public Delete(String url, Consumer<HttpURLConnection> initConnection) {
        super(HttpMethod.DELETE, url);

        init(initConnection);
        connect();
    }

    /**
     * Sends a DELETE request to the specified URL with an authorization token.
     * Returns the response as a JSON object.
     *
     * @param url   The URL to send the DELETE request to
     * @param token The authorization token to include in the request header
     * @return The response parsed as a JSON map
     */
    public static Map<String, Object> api(String url, String token) {
        return api(url, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    /**
     * Sends a DELETE request to the specified URL with custom connection initialization.
     * Returns the response as a JSON object.
     *
     * @param url            The URL to send the DELETE request to
     * @param initConnection Consumer that configures the HTTP connection
     * @return The response parsed as a JSON map
     */
    public static Map<String, Object> api(String url, Consumer<HttpURLConnection> initConnection) {
        return new Delete(url, initConnection).getResp().responseAsJson();
    }

    /**
     * Sends a DELETE request to the specified URL with an authorization token.
     * Returns the response mapped to the specified class type.
     *
     * @param url   The URL to send the DELETE request to
     * @param clz   The class to map the response to
     * @param token The authorization token to include in the request header
     * @return The response mapped to the specified class type
     */
    public static <T> T api(String url, Class<T> clz, String token) {
        return api(url, clz, conn -> conn.setRequestProperty(AUTHORIZATION, "Bearer " + token));
    }

    /**
     * Sends a DELETE request to the specified URL with custom connection initialization.
     * Returns the response mapped to the specified class type.
     *
     * @param url            The URL to send the DELETE request to
     * @param clz            The class to map the response to
     * @param initConnection Consumer that configures the HTTP connection
     * @return The response mapped to the specified class type
     */
    public static <T> T api(String url, Class<T> clz, Consumer<HttpURLConnection> initConnection) {
        return new Delete(url, initConnection).getResp().responseAsBean(clz);
    }
}