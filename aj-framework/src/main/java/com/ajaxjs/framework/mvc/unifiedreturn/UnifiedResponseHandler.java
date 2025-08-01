package com.ajaxjs.framework.mvc.unifiedreturn;

//import com.ajaxjs.desensitize.DeSensitize;
//import com.ajaxjs.desensitize.annotation.Desensitize;

import com.ajaxjs.framework.mvc.filter.RequestLogger;
import com.ajaxjs.security.traceid.TraceXFilter;
import com.ajaxjs.sqlman.util.PrettyLogger;
import com.ajaxjs.util.BoxLogger;
import com.ajaxjs.util.DateHelper;
import com.ajaxjs.util.JsonUtil;
import com.ajaxjs.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RestControllerAdvice
@Slf4j
public class UnifiedResponseHandler implements ResponseBodyAdvice<Object> {
    //不支持的类型列表
    private static final Set<Class<?>> NO_SUPPORTED_CLASSES = new HashSet<>(8);

    static {
        NO_SUPPORTED_CLASSES.add(byte[].class);
        NO_SUPPORTED_CLASSES.add(javax.xml.transform.Source.class);
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        //如果返回值是NO_SUPPORTED_CLASSES中的类型，则不会被当前类的beforeBodyWrite方法处理，即不会被包装为ResultDto类型
        if (NO_SUPPORTED_CLASSES.contains(returnType.getParameterType()))
            return false;

        // 若加了 @IgnoredGlobalReturn 则该方法不用做统一的拦截
//        AnnotatedElement annotatedElement = returnType.getParameterType();
//        if (annotatedElement.isAnnotationPresent(IgnoredGlobalReturn.class))
//            return false;

        if (returnType.hasMethodAnnotation(IgnoredGlobalReturn.class) || returnType.getContainingClass().isAnnotationPresent(IgnoredGlobalReturn.class))
            return false;

        return true;
    }

    private static final String OK = "操作成功";

    @Autowired(required = false)
    CustomReturnConverter<?> customReturnConverter;

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Method method = returnType.getMethod();
        assert method != null;

//        if (method.isAnnotationPresent(Desensitize.class))
//            body = DeSensitize.acquire(body);

        ResponseResultWrapper responseResult = new ResponseResultWrapper();
        boolean isOk;
        int statusCode = ((ServletServerHttpResponse) response).getServletResponse().getStatus(); // Get the HTTP status code

        if (statusCode != 200) {
            responseResult.setStatus(0);
            responseResult.setErrorCode(String.valueOf(statusCode));
            isOk = false;
        } else {
            responseResult.setStatus(1);
            isOk = true;
        }

        JsonMessage annotation = Objects.requireNonNull(returnType.getMethod()).getAnnotation(JsonMessage.class);

        if (annotation != null)
            responseResult.setMessage(annotation.value());
        else
            responseResult.setMessage(isOk ? OK : "操作失败");

        if (body instanceof ResponseResultWrapper)
            BeanUtils.copyProperties(body, responseResult);
        else
            responseResult.setData(body);

        responseResult.setTraceId(MDC.get(BoxLogger.TRACE_KEY));

        try {
            logRequestCompletion(request, responseResult);
            MDC.clear();
        } catch (Throwable e) {
            log.warn("logRequestCompletion", e);
        }

        if (customReturnConverter != null) {
            return customReturnConverter.convert(responseResult);
        }

        return responseResult;
    }

    public static void logRequestCompletion(ServerHttpRequest req, Object responseResult) {
        if (!(req instanceof ServletServerHttpRequest))
            return;

        HttpServletRequest request = ((ServletServerHttpRequest) req).getServletRequest();

        String title = " Request Completion ";
        String sb = "\n" + PrettyLogger.ANSI_BLUE + RequestLogger.boxLine('┌', '─', '┐', title) + '\n' +
                RequestLogger.boxContent("Time:            ", DateHelper.now()) + '\n' +
                RequestLogger.boxContent("TraceId:         ", MDC.get(BoxLogger.TRACE_KEY)) + '\n' +
                RequestLogger.boxContent("Request URI:     ", req.getMethod() + " " + request.getRequestURI()) + '\n' +
                RequestLogger.boxContent("Response Result: ", JsonUtil.toJson(responseResult)) + '\n' +
                RequestLogger.boxContent("Execution Time:  ", getExecutionTime(request)) + '\n' +
                RequestLogger.boxLine('└', '─', '┘', StrUtil.EMPTY_STRING) + PrettyLogger.ANSI_RESET;

        log.info(sb);
    }

    static String getExecutionTime(HttpServletRequest request) {
        Object o = request.getAttribute(RequestLogger.START_TIME_ATTRIBUTE);

        if (o == null)
            return "N/A";
        else {
            long executionTime = System.currentTimeMillis() - (Long) o;

            return executionTime + "ms";
        }
    }

    // Helper method to build error responses
    private Object handleErrorResponse(int status, String message) {
        ResponseResultWrapper errorResponse = new ResponseResultWrapper();
        errorResponse.setStatus(0);
        errorResponse.setMessage(message);
        errorResponse.setData(null); // No data for error

        return errorResponse;
    }
}
