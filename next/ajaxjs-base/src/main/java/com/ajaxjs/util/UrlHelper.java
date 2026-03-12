package com.ajaxjs.util;

import com.ajaxjs.util.io.DataReader;

import java.io.IOException;
import java.net.URL;

/**
 * Utility class for URL manipulation and simple HTTP operations.
 * Provides methods for URL concatenation and basic HTTP GET requests.
 */
public class UrlHelper {
    /**
     * Concatenates two URL parts while ensuring proper formatting.
     * Handles leading and trailing slashes to create a valid URL path.
     * <p>
     * Concatenates two URL directory strings, handling forward slashes appropriately.
     * If one has a trailing slash and the other has a leading slash, it removes one.
     * If neither has a slash, it adds one between them.
     *
     * @param url1 The base URL part
     * @param url2 The URL part to append
     * @return A properly formatted concatenated URL
     */
    public static String concatUrl(String url1, String url2) {
        if (url1.endsWith("/"))
            url1 = url1.substring(0, url1.length() - 1);

        if (url2.startsWith("/"))
            return url1 + url2;
        else
            return url1 + "/" + url2;
    }

    /**
     * Performs a simple HTTP GET request using the raw API and returns the response as text.
     * This method provides basic HTTP functionality without additional dependencies.
     *
     * @param url The target URL to request
     * @return The response content (HTML, JSON, etc.), or null if an error occurs
     */
    public static String simpleGET(String url) {
        try {
            return new DataReader(new URL(url).openStream()).readAsString();
        } catch (IOException e) {
            return null;
        }
    }
}