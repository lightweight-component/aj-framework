package com.ajaxjs.mcp.jsonrpc.model.resources;

/**
 * Base for objects that include optional annotations for the client. The client can
 * use annotations to inform how objects are used or displayed
 */
public interface Annotated {
    Annotations getAnnotations();
}
