package com.ajaxjs.framework.mvc.filter;

import com.ajaxjs.framework.mvc.unifiedreturn.BizAction;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.spring.traceid.TraceXFilter;
import com.ajaxjs.util.CommonConstant;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.date.DateTools;
import com.ajaxjs.util.log.EnableOperationLog;
import com.ajaxjs.util.log.TextBox;
import com.ajaxjs.util.log.Trace;
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
public class RequestLogger implements HandlerInterceptor {
    public static final String START_TIME_ATTRIBUTE = "startTime";

    public static final String TRUE = "true";

    private static final String SILENT_LOG = "silent_log";

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        // avoid the lots of logs shown, BE CAREFULLY to use for missing the logs
        String silentLog = req.getParameter(SILENT_LOG);

        if (TRUE.equals(silentLog) || !(handler instanceof HandlerMethod))
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
        StringBuilder sb = new StringBuilder();
        Map<String, String[]> parameterMap = req.getParameterMap();

        if (!parameterMap.isEmpty()) {
            for (String key : parameterMap.keySet())
                sb.append(key).append("=").append(Arrays.toString(parameterMap.get(key))).append(" ");
        }

        BizAction bizAction = DiContextUtil.getAnnotationFromMethod(handlerMethod, BizAction.class);
        EnableOperationLog enableOperationLog = DiContextUtil.getAnnotationFromMethod(handlerMethod, EnableOperationLog.class);

        if (bizAction != null)
            MDC.put(Trace.BIZ_ACTION, bizAction.value());

        if (enableOperationLog != null)
            MDC.put(Trace.ENABLE_OPERATION_LOG, CommonConstant.EMPTY_STRING);

        String bizActionName = bizAction != null ? bizAction.value() : "unknown";
        String httpInfo = req.getMethod() + " " + req.getRequestURI();
        String controllerInfo = handlerMethod.toString();
        String body = TraceXFilter.getRequestBody(req);

        printLog(httpInfo, bizActionName, null, sb.toString(), body, controllerInfo);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String log = MDC.get(Trace.ENABLE_OPERATION_LOG);

        if (log != null && !log.equals(CommonConstant.EMPTY_STRING)) {

        }
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
    }

    /**
     * 打印请求日志
     *
     * @param httpInfo SQL 语句
     * @param ip       实际执行SQL（带参数）
     * @param params   参数（字符串，或者拼接好的参数描述）
     */
    public static void printLog(String httpInfo, String bizActionName, String ip, String params, String body, String controllerInfo) {
        String title = " Request Information ";
        TextBox box = new TextBox();
        String _log = box.boxStart(" Request Information ")
                .line("Time:", DateTools.now())
                .line("TraceId:    ", MDC.get(Trace.TRACE_KEY))
                .line("BizAction:  ", bizActionName)
                .line("Request:    ", httpInfo)
                .line("IP:         ", ObjectHelper.hasText(ip) ? params : TextBox.NONE)
                .line("Params:     ", ObjectHelper.hasText(params) ? params : TextBox.NONE)
                .line("Body:       ", ObjectHelper.hasText(body) ? body : TextBox.NONE)
                .line("Controller: ", controllerInfo)
                .boxEnd();

        Trace.saveLogToMDC(_log);
        log.info(_log);
    }
}