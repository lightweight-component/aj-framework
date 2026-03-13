package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.function.Consumer;

/**
 * Represents an HTTP PUT request implementation.
 * Extends BasePost to provide specific functionality for PUT HTTP method operations.
 * This class is used for sending HTTP PUT requests typically used for updating resources.
 */
public class Put extends BasePost {
    /**
     * Creates a new Put request with specified HTTP method and URL.
     * This constructor allows for creating custom PUT subclasses if needed.
     *
     * @param method the HTTP method to use (typically PUT)
     * @param url    the URL to which the request will be sent
     */
    public Put(HttpMethod method, String url) {
        super(method, url);
    }

    /**
     * Creates a new Put request with complete configuration.
     *
     * @param url            the URL to which the request will be sent
     * @param data           the request body data
     * @param contentType    the Content-Type header value
     * @param initConnection consumer for custom connection initialization
     */
    public Put(String url, Object data, String contentType, Consumer<HttpURLConnection> initConnection) {
        super(HttpMethod.PUT, url, data, contentType, initConnection);
    }

    /**
     * Creates a new Put request with URL, data and content type.
     * Uses default connection initialization.
     *
     * @param url         the URL to which the request will be sent
     * @param data        the request body data
     * @param contentType the Content-Type header value
     */
    public Put(String url, Object data, String contentType) {
        super(HttpMethod.PUT, url, data, contentType);
    }

    /**
     * Sends a PUT request to the specified API endpoint using JSON format.
     * Returns the raw Response object for further processing.
     *
     * @param url  the API endpoint URL
     * @param data the request payload data to be converted to JSON
     * @return the raw Response object containing the server response
     */
    public static Response api(String url, Object data) {
        return new Put(url, data, CONTENT_TYPE_JSON).getResp();
    }

    /**
     * Sends a PUT request to the specified API endpoint using JSON format
     * with custom connection initialization.
     * Returns the raw Response object for further processing.
     *
     * @param url            the API endpoint URL
     * @param data           the request payload data to be converted to JSON
     * @param initConnection consumer for customizing the HTTP connection before sending
     * @return the raw Response object containing the server response
     */
    public static Response api(String url, Object data, Consumer<HttpURLConnection> initConnection) {
        return new Put(url, data, CONTENT_TYPE_JSON, initConnection).getResp();
    }
}