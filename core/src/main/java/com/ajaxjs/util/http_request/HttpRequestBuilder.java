package com.ajaxjs.util.http_request;

import com.ajaxjs.util.http_request.model.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HttpRequestBuilder implements InvocationHandler {
    private final Class target;

    public HttpRequestBuilder(Class target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Annotation annotation = target.getAnnotation(HttpRequest.class);

        if (annotation == null)
            throw new IllegalArgumentException("Lack of annotation 'HttpRequest'");

        HttpRequest h = (HttpRequest) annotation;
        String prefix = h.url();

        // The return type of the method
        Class<?> returnType = method.getReturnType();

        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping anno = method.getAnnotation(GetMapping.class);
            String url = prefix + anno.value()[0];
            ResponseEntity responseEntity = Get.get(url);

            if (returnType == String.class)
                return responseEntity.toString();
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping anno = method.getAnnotation(PostMapping.class);
            String url = prefix + anno.value()[0];
            ResponseEntity responseEntity = Get.get(url);

            if (returnType == String.class)
                return responseEntity.toString();
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping anno = method.getAnnotation(PutMapping.class);
            String url = prefix + anno.value()[0];
            ResponseEntity responseEntity = Get.get(url);

            if (returnType == String.class)
                return responseEntity.toString();
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping anno = method.getAnnotation(DeleteMapping.class);
            String url = prefix + anno.value()[0];
            ResponseEntity responseEntity = Get.get(url);

            if (returnType == String.class)
                return responseEntity.toString();
        }

        return null;
    }

    public static <T> T build(Class<T> target) {
        return (T) Proxy.newProxyInstance(target.getClassLoader(), new Class[]{target}, new HttpRequestBuilder(target));
    }
}
