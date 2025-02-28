package com.ajaxjs.api.limit;

import com.ajaxjs.api.InterceptorAction;

import javax.servlet.http.HttpServletRequest;

public class LimitAccess extends InterceptorAction<LimitAccessVerify> {
    @Override
    public boolean action(LimitAccessVerify annotation, HttpServletRequest req) {
        return false;
    }
}
