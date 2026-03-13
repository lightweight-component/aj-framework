package com.ajaxjs.util.httpremote.call;

import com.ajaxjs.util.httpremote.call.annotation.GET;
import com.ajaxjs.util.httpremote.call.annotation.Url;

import java.util.Map;

@Url("https://httpbin.org/get")
public interface DoRequest extends BaseCall {
    @GET
    String get();

    @GET("?foo=hi")
    Map<String, Object> getJson();

    @GET(value = "?foo=hi", initConnection = SetHeaders.class)
    Map<String, Object> getJsonWithHeaders();
}
