package com.ajaxjs.util.httpremote;

import com.ajaxjs.util.ObjectHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestPut {
    static final String URL = "https://httpbin.org/put";

    @Test
    void testFormPut() {
        Put put;
        put = new Put(URL, "a=1&b=2&c=3", HttpConstant.CONTENT_TYPE_FORM);
        System.out.println(put.getResp().getResponseText());

        assertTrue(put.getResp().responseAsJson().containsKey("form"));

        put = new Put(URL, ObjectHelper.mapOf("a", 1, "b", "2", "c", "foo"), HttpConstant.CONTENT_TYPE_FORM);

        assertTrue(put.getResp().responseAsJson().containsKey("form"));

        put = new Put(URL, "{\"a\":1,\"b\":\"2\"}", HttpConstant.CONTENT_TYPE_FORM, conn -> {
        });
        assertTrue(put.getResp().responseAsJson().containsKey("form"));

        TestPost.Foo foo = new TestPost.Foo();
        foo.setA("Hi");
        put = new Put(URL, foo, HttpConstant.CONTENT_TYPE_FORM, conn -> {
        });
        assertTrue(put.getResp().responseAsJson().containsKey("form"));
    }

    @Test
    void tesRawJsonPut() {
        Put put = new Put(URL, "a=1&b=2&c=3", HttpConstant.CONTENT_TYPE_JSON);
        System.out.println(put.getResp().getResponseText());

        assertTrue(put.getResp().responseAsJson().containsKey("form"));

        put = new Put(URL, ObjectHelper.mapOf("a", 1, "b", "2", "c", "foo"), HttpConstant.CONTENT_TYPE_JSON);

        assertTrue(put.getResp().responseAsJson().containsKey("form"));

        // note that data is Json, it is not a valid Json because it is missing the quotes around the first key 'b'
        // However, in Raw Body post, it's ok, only if the server can accept it
        put = new Put(URL, "{a:1,\"b\":\"2\"}", HttpConstant.CONTENT_TYPE_JSON, conn -> {
        });
        assertTrue(put.getResp().responseAsJson().containsKey("form"));

        TestPost.Foo foo = new TestPost.Foo();
        foo.setA("Hi");
        put = new Put(URL, foo, HttpConstant.CONTENT_TYPE_JSON, conn -> {
        });
        assertTrue(put.getResp().responseAsJson().containsKey("form"));
    }
}
