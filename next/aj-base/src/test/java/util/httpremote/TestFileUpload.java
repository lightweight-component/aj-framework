package com.ajaxjs.util.httpremote;

import org.junit.jupiter.api.Test;

import java.util.Map;

class TestFileUpload {
    @Test
    void test() {
        Map<String, Object> result = FileUpload.uploadFile("https://httpbin.org/post", "file", "test.txt", "hello world".getBytes(), null);
        System.out.println(result);
    }
}
