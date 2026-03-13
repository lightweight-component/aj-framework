package com.ajaxjs.util.httpremote.call;

import lombok.Data;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class MyHttpApiConfig implements HttpApiConfig {
    @Override
    public Consumer<HttpURLConnection> initConn() {
        return null;
    }

    @Override
    public <T> Function<Map<String, Object>, T> initUnifiedReturn() {
        return resultMap -> {
            MyResponse resp = new MyResponse();

            return (T) resp;
        };
    }

    @Data
    static class MyResponse {
        int code;

        String msg;

        Object data;
    }
}
