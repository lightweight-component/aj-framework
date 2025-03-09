package com.ajaxjs.springboot;

//import com.ajaxjs.desensitize.DeSensitize;
//import com.ajaxjs.desensitize.annotation.Desensitize;
import com.ajaxjs.springboot.annotation.IgnoredGlobalReturn;
import com.ajaxjs.springboot.annotation.JsonMessage;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@RestControllerAdvice
@Component
public class GlobalResponseResult implements ResponseBodyAdvice<Object> {
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
//
//        if (annotatedElement.isAnnotationPresent(IgnoredGlobalReturn.class))
//            return false;

        return true;
    }

    private static final String OK = "操作成功";

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Method method = returnType.getMethod();
        assert method != null;

//        if (method.isAnnotationPresent(Desensitize.class))
//            body = DeSensitize.acquire(body);

        if (method.isAnnotationPresent(IgnoredGlobalReturn.class))
            return body;

        ResponseResultWrapper responseResult = new ResponseResultWrapper();
        responseResult.setStatus(1);

        JsonMessage annotation = Objects.requireNonNull(returnType.getMethod()).getAnnotation(JsonMessage.class);

        if (annotation != null)
            responseResult.setMessage(annotation.value());
        else
            responseResult.setMessage(OK);

        responseResult.setData(body);

        return responseResult;
    }
}
