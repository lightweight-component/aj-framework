package com.ajaxjs.util.http_request;

import org.springframework.web.bind.annotation.GetMapping;

@HttpRequest
public interface FooApi {
    @GetMapping("https://www.baidu.com")
    String testBaidu();
}
