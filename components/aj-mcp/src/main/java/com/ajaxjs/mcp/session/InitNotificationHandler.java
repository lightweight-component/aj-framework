package com.ajaxjs.mcp.session;

/**
 * Notification handler for the initialization notification from the client.
 */
public interface InitNotificationHandler {
    /**
     * Specifies an action to take upon successful initialization.
     *
     * a Mono that will complete when the initialization is acted upon.
     */
    void handle();

}