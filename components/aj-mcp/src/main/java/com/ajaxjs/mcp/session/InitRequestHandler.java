package com.ajaxjs.mcp.session;

import com.ajaxjs.mcp.jsonrpc.model.request.InitializeRequest;
import com.ajaxjs.mcp.jsonrpc.model.request.InitializeResult;

/**
 * Request handler for the initialization request.
 */
public interface InitRequestHandler {
    /**
     * Handles the initialization request.
     *
     * @param initializeRequest the initialization request by the client
     * @return a Mono that will emit the result of the initialization
     */
    InitializeResult handle(InitializeRequest initializeRequest);
}