package com.ajaxjs.framework.mvc.filter;

import com.ajaxjs.framework.cache.Cache;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RandomTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在 Spring MVC 底层内部，当一个请求处理完成以后会发布 ServletRequestHandledEvent 事件，通过监听该事件就能获取请求的详细信息。
 * 类似地有 Servlet 的 ServletRequestListener 事件
 */
//@Component
@Slf4j
public class ShareSession implements Filter {
    public static final String SESSION_ID = "AJ-SESSION-ID";
    public static final String SHARE_SESSION = "SHARE_SESSION";
    public static final String CACHE_PREFIX = "S_ID:";
    public static final int SESSION_TIMEOUT = 3600 * 24 * 3;

    @Autowired
    Cache<String, Object> sessionCache;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            Cookie cookie = WebUtils.getCookie(request, SESSION_ID);

            if (cookie == null)  // 第一次访问还未建立 Session
                setNewSession(request, servletResponse);
            else {
                String sessionId = cookie.getValue();

                if (ObjectHelper.hasText(sessionId)) {
                    Object o = sessionCache.get(CACHE_PREFIX + sessionId);

                    if (o != null) {
                        Map<String, Object> attribute = (Map<String, Object>) o;
                        request.setAttribute(SHARE_SESSION, attribute);
                        // 滑动过期：刷新 TTL
                        sessionCache.put(CACHE_PREFIX + sessionId, attribute, SESSION_TIMEOUT);// 往后延
                    } else
                        setNewSession(request, servletResponse); // Session 过期，创建新的
                }
            }
        } catch (Exception e) {
            log.error("Share Session Error.", e);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void setNewSession(HttpServletRequest request, ServletResponse servletResponse) {
        String sessionId = RandomTools.uuidStr();
        Map<String, Object> attribute = new ConcurrentHashMap<>();
        sessionCache.put(CACHE_PREFIX + sessionId, attribute, SESSION_TIMEOUT);
        request.setAttribute(SHARE_SESSION, attribute);

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        ResponseCookie nCookie = ResponseCookie.from(SESSION_ID, sessionId)
                .httpOnly(false)
                .secure(false)  // HTTPS
                .sameSite("Lax")
                .path("/")
                .maxAge(SESSION_TIMEOUT)  // 与 Redis 一致
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, nCookie.toString());
    }

    public void invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> session = getSession(request);

        if (session != null) {
            Cookie cookie = WebUtils.getCookie(request, SESSION_ID);

            if (cookie != null) {
                String sessionId = cookie.getValue();

                if (ObjectHelper.hasText(sessionId))
                    sessionCache.remove(CACHE_PREFIX + sessionId);
            }
        }

        // 清除 Cookie
        ResponseCookie clearedCookie = ResponseCookie.from(SESSION_ID, "")
                .path("/")
                .maxAge(0) // 立即过期
                .httpOnly(false)
                .secure(false)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearedCookie.toString());
    }

    public static Map<String, Object> getSession(HttpServletRequest request) {
        Object attribute = request.getAttribute(SHARE_SESSION);

        if (attribute == null) {
            log.warn("No share session");
            return null;
        } else
            return (Map<String, Object>) attribute;
    }
}
