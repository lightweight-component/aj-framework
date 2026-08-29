package com.ajaxjs.security.encryptedbody;

/**
 * Represents the iresponse result component.
 */
public interface IResponseResult {
    /**
     * Gets the response payload.
     *
     * @return the response payload
     */
    Object getData();

    /**
     * Sets the response payload.
     *
     * @param data the response payload
     */
    void setData(Object data);
}
