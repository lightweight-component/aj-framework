package com.ajaxjs.framework.mvc.filter;

import com.ajaxjs.framework.mvc.unifiedreturn.BizAction;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.spring.traceid.TraceXFilter;
import com.ajaxjs.util.BoxLogger;
import com.ajaxjs.util.DateHelper;
import com.ajaxjs.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
public class RequestLogger extends BoxLogger implements HandlerInterceptor {
    public static final String START_TIME_ATTRIBUTE = "startTime";

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
//        log.info("请求 URL：{} 对应的控制器方法：{}", req.getRequestURL(), handlerMethod);
        StringBuilder sb = new StringBuilder();
        Map<String, String[]> parameterMap = req.getParameterMap();

        if (!parameterMap.isEmpty()) {
            for (String key : parameterMap.keySet())
                sb.append(key).append("=").append(Arrays.toString(parameterMap.get(key))).append(" ");

//            log.info("{} 请求参数：\n{}", req.getMethod(), s);
        }

        BizAction bizAction = DiContextUtil.getAnnotationFromMethod(handlerMethod, BizAction.class);

        if (bizAction != null)
            MDC.put(BoxLogger.BIZ_ACTION, bizAction.value());

        String bizActionName = bizAction != null ? bizAction.value() : "unknown";
        String httpInfo = req.getMethod() + " " + req.getRequestURI();
        String controllerInfo = handlerMethod.toString();
        String body = TraceXFilter.getRequestBody(req);

        printLog(httpInfo, bizActionName, null, sb.toString(), body, controllerInfo);
    }

//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
//        Object o = request.getAttribute(START_TIME_ATTRIBUTE);
//
//        if (o == null)
//            return;
//
//        long totalExecutionTime = System.currentTimeMillis() - (Long) o;
//
//        if (ex != null)
//            log.error("Request URI: {} - Completed with exception: {} - Total Time: {}ms", request.getRequestURI(), ex.getMessage(), totalExecutionTime);
//        else
//            log.info("Request URI: {} - Total Execution Time: {}ms", request.getRequestURI(), totalExecutionTime);
//    }

    /**
     * 打印数据库操作日志
     *
     * @param httpInfo SQL 语句
     * @param ip       实际执行SQL（带参数）
     * @param params   参数（字符串，或者拼接好的参数描述）
     */
    public static void printLog(String httpInfo, String bizActionName, String ip, String params, String body, String controllerInfo) {
        String title = " Request Information ";
        String sb = "\n" + ANSI_YELLOW + boxLine('┌', '─', '┐', title) + '\n' +
                boxContent("Time:       ", DateHelper.now()) + '\n' +
                boxContent("TraceId:    ", MDC.get(BoxLogger.TRACE_KEY)) + '\n' +
                boxContent("BizAction:  ", bizActionName) + '\n' +
                boxContent("Request:    ", httpInfo) + '\n' +
                boxContent("IP:         ", StrUtil.hasText(ip) ? params : "unknown") + '\n' +
                boxContent("Params:     ", StrUtil.hasText(params) ? params : NONE) + '\n' +
                boxContent("Body:       ", StrUtil.hasText(body) ? body : NONE) + '\n' +
                boxContent("Controller: ", controllerInfo) + '\n' +
                boxLine('└', '─', '┘', StrUtil.EMPTY_STRING) + ANSI_RESET;

        log.info(sb);
    }


}