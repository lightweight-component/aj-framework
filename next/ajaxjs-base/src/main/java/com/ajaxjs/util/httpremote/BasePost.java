package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Abstract base class for HTTP POST requests, handling different types of POST data and connection initialization.
 * Abstract base class for HTTP POST requests, extending Request class,
 * providing functionality for handling various types of POST request data.
 * Supports multiple data formats including JSON, form data, byte arrays, and Java objects.
 */
public abstract class BasePost extends Request {
    /**
     * Creates a new BasePost instance.
     *
     * @param method HTTP method type
     * @param url    the URL for the request
     */
    public BasePost(HttpMethod method, String url) {
        super(method, url);
    }

    /**
     * Creates a new BasePost instance with request data and connection initializer.
     *
     * @param method         HTTP method type
     * @param url            the URL for the request
     * @param data           request data
     * @param initConnection connection initializer for customizing connection settings
     */
    public BasePost(HttpMethod method, String url, Object data, Consumer<HttpURLConnection> initConnection) {
        this(method, url, data, null, initConnection);
    }

    /**
     * Creates a new BasePost instance with request data, content type, and connection initializer.
     *
     * @param method         HTTP method type
     * @param url            the URL for the request
     * @param data           request data
     * @param contentType    content type, defaults to JSON if null
     * @param initConnection connection initializer for customizing connection settings
     */
    @SuppressWarnings("unchecked")
    public BasePost(HttpMethod method, String url, Object data, String contentType, Consumer<HttpURLConnection> initConnection) {
        this(method, url);
        contentType = contentType == null ? HttpConstant.CONTENT_TYPE_JSON : contentType;
        this.setContentType(contentType);

        // Automatically add JSON content type header
        if (contentType.equals(HttpConstant.CONTENT_TYPE_JSON)) {
            if (initConnection == null)
                initConnection = conn -> conn.setRequestProperty(CONTENT_TYPE, HttpConstant.CONTENT_TYPE_JSON);
            else
                initConnection = initConnection.andThen(conn -> conn.setRequestProperty(CONTENT_TYPE, HttpConstant.CONTENT_TYPE_JSON));
        }


        // Set request body based on a data type
        if (data != null) {
            if (data instanceof String) {
                String str = (String) data;

                if (isJson(str)) // JSON string
                    this.setData((String) data);
                else if (str.contains("="))// key-value pair string
                    this.setDataStr(str);
            } else if (data instanceof Map)
                this.setData((Map<String, Object>) data);
            else if (data instanceof byte[])
                this.setData((byte[]) data);
            else // Java Bean object
                this.setData(data);
        }

        init(initConnection);
        initData();
        connect();
    }

    /**
     * Creates a new BasePost instance with a request data and content type.
     *
     * @param method      HTTP method type
     * @param url         the URL for the request
     * @param data        request data
     * @param contentType content type
     */
    public BasePost(HttpMethod method, String url, Object data, String contentType) {
        this(method, url, data, contentType, null);
    }

    /**
     * Checks if a string is in JSON format.
     *
     * @param str the string to check
     * @return true if the string is in JSON format, false otherwise
     */
    static boolean isJson(String str) {
        return (str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"));
    }
}