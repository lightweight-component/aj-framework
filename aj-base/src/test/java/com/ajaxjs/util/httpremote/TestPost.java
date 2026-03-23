package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.ObjectHelper;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestPost {
    @Data
    static class Foo {
        private String a;
    }

    @Test
    void testFormPost() {
        Post post = new Post("https://httpbin.org/post", "a=1&b=2&c=3", HttpConstant.CONTENT_TYPE_FORM);
//        System.out.println(post.getResp().getResponseText());

        assertTrue(post.getResp().responseAsJson().containsKey("form"));

        post = new Post("https://httpbin.org/post", ObjectHelper.mapOf("a", 1, "b", "2", "c", "foo"), HttpConstant.CONTENT_TYPE_FORM);

        assertTrue(post.getResp().responseAsJson().containsKey("form"));

        post = new Post("https://httpbin.org/post", "{\"a\":1,\"b\":\"2\"}", HttpConstant.CONTENT_TYPE_FORM, conn -> {
        });
        assertTrue(post.getResp().responseAsJson().containsKey("form"));

        Foo foo = new Foo();
        foo.setA("Hi");
        post = new Post("https://httpbin.org/post", foo, HttpConstant.CONTENT_TYPE_FORM, conn -> {
        });
        assertTrue(post.getResp().responseAsJson().containsKey("form"));
    }

    @Test
    void tesRawJsonPost() {
        Post post = new Post("https://httpbin.org/post", "a=1&b=2&c=3", HttpConstant.CONTENT_TYPE_JSON);
//        System.out.println(post.getResp().getResponseText());

        assertTrue(post.getResp().responseAsJson().containsKey("form"));

        post = new Post("https://httpbin.org/post", ObjectHelper.mapOf("a", 1, "b", "2", "c", "foo"), HttpConstant.CONTENT_TYPE_JSON);

        assertTrue(post.getResp().responseAsJson().containsKey("form"));

        // note that data is Json, it is not a valid Json because it is missing the quotes around the first key 'b'
        // However, in Raw Body post, it's ok, only if the server can accept it
        post = new Post("https://httpbin.org/post", "{\"a\":1,\"b\":\"2\"}", HttpConstant.CONTENT_TYPE_JSON, conn -> {
        });
        assertTrue(post.getResp().responseAsJson().containsKey("form"));

        Foo foo = new Foo();
        foo.setA("Hi");
        post = new Post("https://httpbin.org/post", foo, HttpConstant.CONTENT_TYPE_JSON, conn -> {
        });
        assertTrue(post.getResp().responseAsJson().containsKey("form"));
    }
}
