package com.ajaxjs.util.httpremote.call;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class NoConfig implements HttpApiConfig {
    @Override
    public Consumer<HttpURLConnection> initConn() {
        return null;
    }

    @Override
    public <T> Function<Map<String, Object>, T> initUnifiedReturn() {
        return null;
    }
}
