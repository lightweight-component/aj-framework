package com.ajaxjs.util.httpremote;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/**
 * HTTP HEAD request implementation.
 * This class provides functionality for sending HTTP HEAD requests,
 * which are identical to GET requests but only return headers in the response
 * without the response body. This is useful for retrieving metadata about a resource
 * without transferring the actual resource itself.
 * It extends the base Request class and provides utility methods for header inspection.
 */
public class Head extends Request {
    /**
     * Creates a new Head request with the specified HTTP method and URL.
     *
     * @param method The HTTP method to use
     * @param url    The URL to send the request to
     */
    public Head(HttpMethod method, String url) {
        super(method, url);
    }

    /**
     * Creates a new Head request with the specified URL.
     * Configures the input stream consumer to do nothing since HEAD requests
     * don't have response bodies, which saves resources.
     *
     * @param url The URL to send the HEAD request to
     */
    public Head(String url) {
        super(HttpMethod.HEAD, url);
        // Don't convert response text for HEAD requests to save resources
        this.setInputStreamConsumer(in -> {
        });
    }

    /**
     * Initializes the HTTP connection with special configuration for HEAD requests.
     * Disables automatic redirect following to allow manual handling of redirects.
     *
     * @param initConnection Consumer that configures the HTTP connection
     * @return The initialized HttpURLConnection object
     */
    @Override
    public HttpURLConnection init(Consumer<HttpURLConnection> initConnection) {
        Consumer<HttpURLConnection> beforeInit = conn -> {
            // Must set too false to prevent automatic redirect to Location URL
            conn.setInstanceFollowRedirects(false);
        };

        return super.init(initConnection == null ? beforeInit : beforeInit.andThen(initConnection));
    }

    /**
     * Initializes the HTTP connection with default settings for HEAD requests.
     *
     * @return The initialized HttpURLConnection object
     */
    @Override
    public HttpURLConnection init() {
        return init(null);
    }

    /**
     * Obtains the redirect address from a 302 HTTP response.
     *
     * @return The redirect URL from the Location header
     */
    public String get302redirect() {
        return getConn().getHeaderField("Location");
    }

    /**
     * Checks if the requested resource does not exist (404 Not Found).
     *
     * @return true if the HTTP status code is 404, indicating the resource was not found
     * @throws RuntimeException if an I/O error occurs while retrieving the response code
     */
    public boolean is404() {
        try {
            return getConn().getResponseCode() == 404;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the file size from the Content-Length header.
     *
     * @return The content length in bytes, or -1 if the content length is not known
     */
    public long getFileSize() {
        return getConn().getContentLength();
    }

    /**
     * Checks if the input stream is GZIP-encoded and returns the appropriate input stream.
     * Some websites automatically add Content-Encoding:gzip regardless of the request headers.
     *
     * @param conn HTTP connection object
     * @param in   Original input stream
     * @return GZIPInputStream if Content-Encoding is gzip, otherwise null
     */
    public static InputStream gzip(HttpURLConnection conn, InputStream in) {
        if ("gzip".equals(conn.getHeaderField("Content-Encoding"))) {
            try {
                return new GZIPInputStream(in);
            } catch (IOException e) {
                // Logging commented out in original code
            }
        }

        return null;
    }

    /**
     * Converts a Map of key-value pairs into an HTTP header configuration function.
     * This is a higher-order function that returns a Consumer which can be used
     * to set multiple request properties at once.
     *
     * @param map The map containing header names and values
     * @return A lambda function that applies the header values to an HttpURLConnection
     */
    public static Consumer<HttpURLConnection> map2header(Map<String, ?> map) {
        return conn -> {
            for (String key : map.keySet())
                conn.setRequestProperty(key, map.get(key).toString());
        };
    }

    public static Consumer<HttpURLConnection> json(String token) {
        return conn -> {
            conn.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_JSON);
            conn.setRequestProperty(AUTHORIZATION, "Bearer " + token);
        };
    }

}