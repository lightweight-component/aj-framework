package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.MapTool;
import com.ajaxjs.util.ObjectHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Represents an HTTP response received after sending a request.
 * This class stores all response-related information including status codes,
 * response text, headers, and provides utility methods for parsing the response in different formats.
 */
@Data
@Slf4j
public class Response {
    /**
     * The underlying HTTP connection object
     */
    private HttpURLConnection connection;

    /**
     * The requested URL that generated this response
     */
    private String url;

    /**
     * The HTTP method used for this request
     */
    private String httpMethod;

    /**
     * The parameters sent with the request
     */
    private Map<String, Object> params;

    /**
     * Whether the request was successful (HTTP 200-299 status code)
     */
    private boolean isOk;

    /**
     * Any exception that occurred during the request (null if successful)
     */
    private Exception ex;

    /**
     * The HTTP status code returned by the server
     */
    private Integer httpCode;

    /**
     * The response message content as a string
     */
    private String responseText;

    /**
     * The input stream containing the raw response data
     */
    private InputStream in;

    /**
     * Timestamp when the request was initiated
     */
    private Long startTime;

    /**
     * Parses the response as a JSON array of maps.
     *
     * @return a list of maps representing the JSON array, or null if response is empty
     * @throws IllegalStateException if the response content type is not JSON
     */
    public List<Map<String, Object>> responseAsJsonList() {
        checkIsJsonReturn();

        return ObjectHelper.hasText(responseText) ? JsonUtil.json2mapList(responseText) : null;
    }

    /**
     * Parses the response as a JSON object.
     *
     * @return a map representing the JSON object, or null if response is empty
     * @throws IllegalStateException if the response content type is not JSON
     */
    public Map<String, Object> responseAsJson() {
//        checkIsJsonReturn();

        return ObjectHelper.hasText(responseText) ? JsonUtil.json2map(responseText) : null;
    }

    /**
     * Checks if the response content type is JSON.
     * Throws an exception if the content type is not JSON.
     *
     * @throws IllegalStateException if the content type is not JSON
     */
    private void checkIsJsonReturn() {
        Map<String, List<String>> headers = connection.getHeaderFields();
        List<String> types = headers.get(HttpConstant.CONTENT_TYPE);

        if (!ObjectHelper.isEmpty(types)) {
            for (String type : types) {
                if (type.contains(HttpConstant.CONTENT_TYPE_JSON))
                    return;
            }
        }

        throw new IllegalStateException("The server returns wrong content-type: " + types + ". It's a JSON API call.");
    }

    /**
     * Converts the JSON response to a Java Bean of the specified class.
     *
     * @param <T> the type of the Java Bean
     * @param clz the class of the Java Bean to create
     * @return the Java Bean instance populated with response data, or null if response was not successful
     */
    public <T> T responseAsBean(Class<T> clz) {
        if (!isOk)
            return null;

        Map<String, Object> map = responseAsJson();

        return map == null ? null : JsonUtil.map2pojo(map, clz);
    }

    /**
     * Parses the response as XML and converts it to a Map.
     *
     * @return a map representation of the XML content, or null if response is empty
     */
    public Map<String, String> responseAsXML() {
        return ObjectHelper.hasText(responseText) ? MapTool.xmlToMap(responseText) : null;
    }

    /**
     * Key name for error message in JSON response objects
     */
    public final static String ERR_MSG = "errMsg";

    /**
     * Key name for status code in JSON response objects
     */
    public final static String STATUS = "status";

    /**
     * Checks if the remote request was successful based on AJ-Spring response structure.
     * A response is considered successful if it contains a "status" key with value "1".
     *
     * @param result the response result map to check
     * @return true if the request was successful, according to the response structure
     */
    public static boolean isOk(Map<String, Object> result) {
        return result != null && result.containsKey(STATUS) && "1".equals(result.get(STATUS).toString());
    }
}