package com.ajaxjs.util.httpremote;

import java.net.HttpURLConnection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * HTTP-related constants and utilities for the HTTP client implementation.
 * This interface defines common HTTP method names, content types,
 * and functional utilities for HTTP connection configuration.
 */
public interface HttpConstant {
    /** HTTP GET method name */
    String GET = "GET";

    /** HTTP POST method name */
    String POST = "POST";

    /** HTTP PUT method name */
    String PUT = "PUT";

    /** HTTP DELETE method name */
    String DELETE = "DELETE";

    /** Content-Type header field name */
    String CONTENT_TYPE = "Content-Type";

    /** JSON content type */
    String CONTENT_TYPE_JSON = "application/json";
    
    /** XML content type */
    String CONTENT_TYPE_XML = "application/xml";

    /** JSON content type with UTF-8 charset */
    String CONTENT_TYPE_JSON_UTF8 = CONTENT_TYPE_JSON + ";charset=utf-8";

    /** Form data content type */
    String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    /** Form data content type with UTF-8 charset */
    String CONTENT_TYPE_FORM_UTF8 = CONTENT_TYPE_FORM + ";charset=utf-8";

    /** Multipart form data content type for file uploads */
    String CONTENT_TYPE_FORM_UPLOAD = "multipart/form-data";
    
    /** Generic binary file content type */
    String FILE_TYPE = "application/octet-stream";
    
    /** Authorization header field name */
    String AUTHORIZATION = "Authorization";

    /**
     * Enumeration of HTTP methods supported by the client.
     * Includes all standard HTTP methods.
     */
    enum HttpMethod {
        GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, CONNECT
    }

    /**
     * Empty connection initializer that does nothing.
     * Used as default when no custom initialization is needed.
     */
    Consumer<HttpURLConnection> EMPTY_INIT = conn -> {
    };

    /**
     * Sets the User-Agent header for client identification.
     * This BiConsumer takes the connection and a URL to set the User-Agent property.
     */
    BiConsumer<HttpURLConnection, String> SET_USER_AGENT = (conn, url) -> conn.addRequestProperty("User-Agent", url);

    /**
     * Sets a default User-Agent header to identify as Mozilla Firefox.
     * This is useful for avoiding blocks from servers that require a valid browser User-Agent.
     */
    Consumer<HttpURLConnection> SET_USER_AGENT_DEFAULT = conn -> SET_USER_AGENT.accept(conn, "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
}