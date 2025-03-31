package org.example.service;

import feign.InvocationContext;
import feign.Response;
import feign.ResponseInterceptor;

import java.io.IOException;

public class CustomResponseInterceptor implements ResponseInterceptor {
    @Override
    public Object aroundDecode(InvocationContext invocationContext) throws IOException {
        System.out.println("CustomResponseInterceptor.aroundDecode");
        Response response = invocationContext.response();

        // Custom logic to handle the response
        if (response.status() == 200) {
            // Log successful response
            System.out.println("Successful response: " + response);
        } else if (response.status() == 404) {
            // Handle 404 Not Found
            System.out.println("Resource not found: " + response.request().url());
        } else {
            // Handle other status codes
            System.out.println("Response status: " + response.status());
        }

        return invocationContext.proceed();
    }
}