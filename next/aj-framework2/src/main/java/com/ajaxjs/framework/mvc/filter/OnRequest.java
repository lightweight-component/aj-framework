package com.ajaxjs.framework.mvc.filter;

import org.springframework.context.event.EventListener;
import org.springframework.web.context.support.ServletRequestHandledEvent;

//@Component
public class OnRequest {
    @EventListener(ServletRequestHandledEvent.class)
    public void onRequest(ServletRequestHandledEvent event) {
    }
}
