package com.ajaxjs.mcp.jsonrpc.model.resources;

import lombok.Data;

/**
 * Sent from the client to request resources/updated notifications from the server whenever a particular resource changes.
 */
@Data
public class SubscribeRequest {
    /**
     * the URI of the resource to subscribe to. The URI can use any protocol; it is up to the server how to interpret it.
     */
    String uri;
}
