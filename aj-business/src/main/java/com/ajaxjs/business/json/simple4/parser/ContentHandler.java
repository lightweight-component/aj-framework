package com.ajaxjs.business.json.simple4.parser;

import java.io.IOException;

/**
 * A simplified and stoppable SAX-like content handler for stream processing of JSON text.
 *
 * @author FangYidong<fangyidong @ yahoo.com.cn>
 * @see org.xml.sax.ContentHandler
 * @see org.json.simple.parser.JSONParser#parse(java.io.Reader, ContentHandler, boolean)
 */
public interface ContentHandler {
    /**
     * Receive notification of the beginning of JSON processing.
     * The parser will invoke this method only once.
     * <p>
     * - JSONParser will stop and throw the same exception to the caller when receiving this exception.
     */
    void startJSON() throws ParseException, IOException;

    /**
     * Receive notification of the end of JSON processing.
     */
    void endJSON() throws ParseException, IOException;

    /**
     * Receive notification of the beginning of a JSON object.
     *
     * @return false if the handler wants to stop parsing after return.
     * - JSONParser will stop and throw the same exception to the caller when receiving this exception.
     * @see #endJSON
     */
    boolean startObject() throws ParseException, IOException;

    /**
     * Receive notification of the end of a JSON object.
     *
     * @return false if the handler wants to stop parsing after return.
     * @see #startObject
     */
    boolean endObject() throws ParseException, IOException;

    /**
     * Receive notification of the beginning of a JSON object entry.
     *
     * @param key - Key of a JSON object entry.
     * @return false if the handler wants to stop parsing after return.
     * @see #endObjectEntry
     */
    boolean startObjectEntry(String key) throws ParseException, IOException;

    /**
     * Receive notification of the end of the value of previous object entry.
     *
     * @return false if the handler wants to stop parsing after return.
     * @see #startObjectEntry
     */
    boolean endObjectEntry() throws ParseException, IOException;

    /**
     * Receive notification of the beginning of a JSON array.
     *
     * @return false if the handler wants to stop parsing after return.
     * @see #endArray
     */
    boolean startArray() throws ParseException, IOException;

    /**
     * Receive notification of the end of a JSON array.
     *
     * @return false if the handler wants to stop parsing after return.
     * @see #startArray
     */
    boolean endArray() throws ParseException, IOException;

    /**
     * Receive notification of the JSON primitive values:
     * java.lang.String,
     * java.lang.Number,
     * java.lang.Boolean
     * null
     *
     * @param value - Instance of the following:
     *              java.lang.String,
     *              java.lang.Number,
     *              java.lang.Boolean
     *              null
     * @return false if the handler wants to stop parsing after return.
     */
    boolean primitive(Object value) throws ParseException, IOException;

}
