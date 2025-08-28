package com.ajaxjs.framework.mvc.filter;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletRequestHandledEvent;

/**
 * 在 Spring MVC 底层内部，当一个请求处理完成以后会发布 ServletRequestHandledEvent 事件，通过监听该事件就能获取请求的详细信息。
 * 类似地有 Servlet 的 ServletRequestListener 事件
 */
//@Component
public class OnRequest {
    @EventListener(ServletRequestHandledEvent.class)
    public void recordTimed(ServletRequestHandledEvent event) {
    }
}
