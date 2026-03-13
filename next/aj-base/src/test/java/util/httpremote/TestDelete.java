package com.ajaxjs.util.httpremote;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDelete {
    @Test
    void testDelete() {
        String url = "https://httpbin.org/delete";
        Map<String, Object> response = Delete.api(url, conn -> {
        });

        assertTrue(response.containsKey("headers"));
    }

    @Test
    void testDeleteWithToken() {
        String url = "https://api.github.com/repos/ajaxjs/ajaxjs_util/issues/comments/1234567890";
        String token = "your_github_token";
        Map<String, Object> response = Delete.api(url, token);

        assertTrue(response.containsKey("message"));
    }
}
