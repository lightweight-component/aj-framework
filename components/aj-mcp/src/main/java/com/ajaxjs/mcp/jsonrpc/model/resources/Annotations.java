package com.ajaxjs.mcp.jsonrpc.model.resources;

import com.ajaxjs.mcp.jsonrpc.model.Role;

import java.util.List;

/**
 * Optional annotations for the client. The client can use annotations to inform how objects are used or displayed.
 */
public class Annotations {
    /**
     * Describes who the intended customer of this object or data is. It
     * can include multiple entries to indicate content useful for multiple audiences
     * (e.g., `["user", "assistant"]`).
     */
    List<Role> audience;

    /**
     * Describes how important this data is for operating the server. A
     * value of 1 means "most important," and indicates that the data is effectively
     * required, while 0 means "least important," and indicates that the data is entirely
     * optional. It is a number between 0 and 1.
     */
    Double priority;
}
