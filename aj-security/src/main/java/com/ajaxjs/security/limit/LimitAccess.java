package com.ajaxjs.security.limit;


import com.ajaxjs.security.InterceptorAction;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Represents the limit access component.
 */
public class LimitAccess extends InterceptorAction<LimitAccessVerify> {
    /**
     * Executes the action operation.
     *
     * @param annotation the annotation parameter.
     * @param req        the req parameter.
     * @return the operation result.
     */
    @Override
    public boolean action(LimitAccessVerify annotation, HttpServletRequest req) {
        return false;
    }
}
