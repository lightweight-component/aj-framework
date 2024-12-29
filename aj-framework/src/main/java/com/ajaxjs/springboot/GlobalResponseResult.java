package com.ajaxjs.springboot;

import com.ajaxjs.desensitize.DeSensitize;
import com.ajaxjs.desensitize.annotation.Desensitize;
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

import java.lang.reflect.Method;
import java.util.Objects;

@RestControllerAdvice
@Component
public class GlobalResponseResult implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
//        System.out.println("supports:" + returnType);
        return true;
    }

    private static final String OK = "操作成功";

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Method method = returnType.getMethod();
        assert method != null;

        if (method.isAnnotationPresent(Desensitize.class))
            body=  DeSensitize.acquire(body);

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
