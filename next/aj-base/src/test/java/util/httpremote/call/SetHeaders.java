package com.ajaxjs.util.httpremote.call;

import java.net.HttpURLConnection;
import java.util.function.Consumer;

public class SetHeaders implements Consumer<HttpURLConnection> {
    @Override
    public void accept(HttpURLConnection conn) {
        conn.setRequestProperty("User-Agent", "Demo");
    }
}
