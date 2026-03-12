package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents an HTTP POST request implementation.
 * Extends BasePost to provide specific functionality for POST HTTP method operations.
 * This class is used for sending HTTP POST requests with various content types and data formats.
 */
public class Post extends BasePost {
    /**
     * Creates a new Post request with specified HTTP method and URL.
     * This constructor allows for creating custom POST subclasses if needed.
     *
     * @param method the HTTP method to use (typically POST)
     * @param url    the URL to which the request will be sent
     */
    public Post(HttpMethod method, String url) {
        super(method, url);
    }

    /**
     * Creates a new Post request with complete configuration.
     *
     * @param url            the URL to which the request will be sent
     * @param data           the request body data
     * @param contentType    the Content-Type header value
     * @param initConnection consumer for custom connection initialization
     */
    public Post(String url, Object data, String contentType, Consumer<HttpURLConnection> initConnection) {
        super(HttpMethod.POST, url, data, contentType, initConnection);
    }

    /**
     * Creates a new Post request with URL, data and content type.
     * Uses default connection initialization.
     *
     * @param url         the URL to which the request will be sent
     * @param data        the request body data
     * @param contentType the Content-Type header value
     */
    public Post(String url, Object data, String contentType) {
        super(HttpMethod.POST, url, data, contentType);
    }

    /**
     * Sends a POST request to the specified API endpoint using JSON format.
     * Automatically converts the response to a Map using JSON parsing.
     *
     * @param url  the API endpoint URL
     * @param data the request payload data to be converted to JSON
     * @return the response as a Map<String, Object> containing parsed JSON data
     */
    public static Map<String, Object> api(String url, Object data) {
        return new Post(url, data, CONTENT_TYPE_JSON).getResp().responseAsJson();
    }

    /**
     * Sends a POST request to the specified API endpoint using JSON format
     * with custom connection initialization.
     * Automatically converts the response to a Map using JSON parsing.
     *
     * @param url            the API endpoint URL
     * @param data           the request payload data to be converted to JSON
     * @param initConnection consumer for customizing the HTTP connection before sending
     * @return the response as a Map<String, Object> containing parsed JSON data
     */
    public static Map<String, Object> api(String url, Object data, Consumer<HttpURLConnection> initConnection) {
        return new Post(url, data, CONTENT_TYPE_JSON, initConnection).getResp().responseAsJson();
    }

    public static Map<String, Object> form(String url, Object data, Consumer<HttpURLConnection> initConnection) {
        return new Post(url, data, CONTENT_TYPE_FORM, initConnection).getResp().responseAsJson();
    }
}