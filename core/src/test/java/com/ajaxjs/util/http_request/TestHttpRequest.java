package com.ajaxjs.util.http_request;

import org.junit.jupiter.api.Test;

public class TestHttpRequest {
    @Test
    public void testGet() {
        FooApi fooApi = HttpRequestBuilder.build(FooApi.class);
        String result = fooApi.testBaidu();
        System.out.println(result);
    }
}
