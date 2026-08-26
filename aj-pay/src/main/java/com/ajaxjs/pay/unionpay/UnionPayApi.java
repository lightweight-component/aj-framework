package com.ajaxjs.pay.unionpay;

import com.ajaxjs.util.MapTool;
import com.ajaxjs.util.httpremote.Post;

import java.util.Map;

public class UnionPayApi {
    public static String authUrl = "https://qr.95516.com/qrcGtwWeb-web/api/userAuth?version=1.0.0&redirectUrl=%s";

    public static Map<String, Object> execution(String url, Map<String, String> params) {
        return Post.api(url, MapTool.mapToXml(params));
    }

    /**
     * 获取用户授权 API
     *
     * @param url 回调地址，可以自定义参数 <a href="https://pay.javen.com/callback?sdk=ijpay">...</a>
     * @return 银联重定向 Url
     */
    public static String buildAuthUrl(String url) {
        return String.format(authUrl, url);
    }
}
