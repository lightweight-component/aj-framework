package com.ajaxjs.framework.mvc.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

/**
 * 全局的控制器拦截器
 * 记录请求入参和执行时间统计
 * 可以参考 Spring 的 CommonsRequestLoggingFilter
 */
@Slf4j
public class RequestLogger implements HandlerInterceptor {
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        if (!(handler instanceof HandlerMethod))
            return true;

        HandlerMethod handlerMethod = (HandlerMethod) handler; // 接口上的方法
        showControllerInfo(req, handlerMethod);

        long startTime = System.currentTimeMillis();
        req.setAttribute(START_TIME_ATTRIBUTE, startTime);

        return true;
    }

    /**
     * 获得 Controller 方法名、请求参数和注解信息
     *
     * @param req           请求对象
     * @param handlerMethod 方法
     */
    private static void showControllerInfo(HttpServletRequest req, HandlerMethod handlerMethod) {
        log.info("请求 URL：{} 对应的控制器方法：{}", req.getRequestURL(), handlerMethod);

        StringBuffer s = new StringBuffer();
        Map<String, String[]> parameterMap = req.getParameterMap();

        if (!parameterMap.isEmpty()) {
            for (String key : parameterMap.keySet())
                s.append(key).append("=").append(Arrays.toString(parameterMap.get(key))).append("\n");

            log.info("{} 请求参数：\n{}", req.getMethod(), s);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Object o = request.getAttribute(START_TIME_ATTRIBUTE);

        if (o == null)
            return;

        long totalExecutionTime = System.currentTimeMillis() - (Long) o;

        if (ex != null)
            log.error("Request URI: {} - Completed with exception: {} - Total Time: {}ms", request.getRequestURI(), ex.getMessage(), totalExecutionTime);
        else
            log.info("Request URI: {} - Total Execution Time: {}ms", request.getRequestURI(), totalExecutionTime);
    }
}