package com.ajaxjs.security.limit;


import com.ajaxjs.security.InterceptorAction;

import jakarta.servlet.http.HttpServletRequest;

public class LimitAccess extends InterceptorAction<LimitAccessVerify> {
    @Override
    public boolean action(LimitAccessVerify annotation, HttpServletRequest req) {
        return false;
    }
}
