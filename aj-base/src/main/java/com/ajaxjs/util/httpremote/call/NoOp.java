package com.ajaxjs.util.httpremote.call;

import java.net.HttpURLConnection;
import java.util.function.Consumer;

public class NoOp implements Consumer<HttpURLConnection> {
    public void accept(HttpURLConnection conn) {
        // no-op
    }
}
