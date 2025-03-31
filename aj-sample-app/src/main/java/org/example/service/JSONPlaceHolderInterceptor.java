package org.example.service;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class JSONPlaceHolderInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        System.out.println("-------------" + requestTemplate);
        requestTemplate.headers();
    }
}
