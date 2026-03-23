package com.ajaxjs.util.httpremote.call;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface HttpApiConfig {
    Consumer<HttpURLConnection> initConn();

    <T> Function<Map<String, Object>, T> initUnifiedReturn();
}
