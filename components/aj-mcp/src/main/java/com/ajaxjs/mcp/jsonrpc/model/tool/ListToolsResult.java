package com.ajaxjs.mcp.jsonrpc.model.tool;

import java.util.List;

/**
 * The server's response to a tools/list request from the client.
 */
public class ListToolsResult {
    /**
     * A list of tools that the server provides.
     */
    List<Tool> tools;

    /**
     * An optional cursor for pagination. If present, indicates there are more tools available.
     */
    String nextCursor;
}
