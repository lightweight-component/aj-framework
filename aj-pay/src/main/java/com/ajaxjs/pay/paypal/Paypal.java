package com.ajaxjs.pay.paypal;

import com.ajaxjs.util.Base64Utils;
import com.ajaxjs.util.ObjectHelper;
import com.ajaxjs.util.RandomTools;
import com.ajaxjs.util.httpremote.Get;
import com.ajaxjs.util.httpremote.Head;
import com.ajaxjs.util.httpremote.HttpConstant;
import com.ajaxjs.util.httpremote.Post;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Paypal implements PayPalApiUrl {
    private AccessToken accessToken;

    private ApiConfig config;

    /**
     * 获取接口请求的 URL
     *
     * @param isSandBox 是否是沙箱环境
     * @return {@link String} 返回完整的接口请求URL
     */
    public static String getReqUrl(boolean isSandBox, String url) {
        return (isSandBox ? PayPalApiUrl.SANDBOX_GATEWAY : PayPalApiUrl.LIVE_GATEWAY).concat(url);
    }

    /**
     * 获取 AccessToken
     *
     * @return {@link IJPayHttpResponse} 请求返回的结果
     */
    public Map<String, Object> getToken() {
        Map<String, String> headers = new HashMap<>(3);
        headers.put("Accept", HttpConstant.CONTENT_TYPE_JSON);
        headers.put("Content-Type", HttpConstant.CONTENT_TYPE_FORM);
        headers.put("Authorization", "Basic ".concat(new Base64Utils(config.getClientId().concat(":").concat(config.getSecret())).encodeAsString()));
        Map<String, Object> params = new HashMap<>(1);
        params.put("grant_type", "client_credentials");

        String url = getReqUrl(config.isSandBox(), GET_TOKEN);

        return Post.api(url, params, Head.map2header(headers));
    }

    /**
     * 创建订单
     *
     * @param data 请求参数
     */
    public Map<String, Object> createOrder(String data) {
        String url = getReqUrl(config.isSandBox(), CHECKOUT_ORDERS);

        return Post.api(url, data, Head.map2header(getBaseHeaders(accessToken)));
    }

    /**
     * 更新订单
     *
     * @param id   订单号
     * @param data 请求参数
     */
    public Map<String, Object> updateOrder(String id, String data) {
        String url = getReqUrl(config.isSandBox(), CHECKOUT_ORDERS).concat("/").concat(id);

//        return patch(url, data, getBaseHeaders(accessToken));
        return null; // todo patch
    }


    /**
     * 查询订单
     *
     * @param orderId 订单号
     * @return {@link IJPayHttpResponse} 请求返回的结果
     */
    public Map<String, Object> queryOrder(String orderId) {
        String url = getReqUrl(config.isSandBox(), CHECKOUT_ORDERS).concat("/").concat(orderId);

        return Get.api(url, Head.map2header(getBaseHeaders(accessToken)));
    }

    /**
     * 确认订单
     *
     * @param id   订单号
     * @param data 请求参数
     * @return {@link IJPayHttpResponse} 请求返回的结果
     */
    public Map<String, Object> captureOrder(String id, String data) {
        String url = String.format(getReqUrl(config.isSandBox(), CAPTURE_ORDER), id);

        return Post.api(url, data, Head.map2header(getBaseHeaders(accessToken)));
    }

    /**
     * 查询确认的订单
     *
     * @param captureId 订单号
     * @return {@link IJPayHttpResponse} 请求返回的结果
     */
    public Map<String, Object> captureQuery(String captureId) {
        String url = String.format(getReqUrl(config.isSandBox(), CAPTURE_QUERY), captureId);

        return Get.api(url, Head.map2header(getBaseHeaders(accessToken)));
    }

    /**
     * 退款
     *
     * @param captureId 订单号
     * @param data      请求参数
     * @return {@link IJPayHttpResponse} 请求返回的结果
     */
    public Map<String, Object> refund(String captureId, String data) {
        String url = String.format(getReqUrl(config.isSandBox(), REFUND), captureId);

        return Post.api(url, data, Head.map2header(getBaseHeaders(accessToken)));
    }

    /**
     * 查询退款
     *
     * @param id 订单号
     * @return {@link IJPayHttpResponse} 请求返回的结果
     */
    public Map<String, Object> refundQuery(String id) {
        String url = String.format(getReqUrl(config.isSandBox(), REFUND_QUERY), id);

        return Get.api(url, Head.map2header(getBaseHeaders(accessToken)));
    }

    /**
     * 简化的UUID，去掉了横线，使用性能更好的 ThreadLocalRandom 生成UUID
     *
     * @return 简化的 UUID，去掉了横线
     */
    public static String generateStr() {
        return RandomTools.uuidStr().replace("-", "");
    }

    public static Map<String, String> getBaseHeaders(AccessToken accessToken) {
        return getBaseHeaders(accessToken, generateStr(), null, null);
    }

    public static Map<String, String> getBaseHeaders(AccessToken accessToken, String payPalRequestId, String payPalPartnerAttributionId, String prefer) {
        if (accessToken == null || ObjectHelper.isEmptyText(accessToken.getTokenType()) || ObjectHelper.isEmptyText(accessToken.getAccessToken()))
            throw new RuntimeException("accessToken is null");

        Map<String, String> headers = new HashMap<>(3);
        headers.put("Content-Type", HttpConstant.CONTENT_TYPE_JSON);
        headers.put("Authorization", accessToken.getTokenType().concat(" ").concat(accessToken.getAccessToken()));

        if (ObjectHelper.hasText(payPalRequestId))
            headers.put("PayPal-Request-Id", payPalRequestId);

        if (ObjectHelper.hasText(payPalPartnerAttributionId))
            headers.put("PayPal-Partner-Attribution-Id", payPalPartnerAttributionId);

        if (ObjectHelper.hasText(prefer))
            headers.put("Prefer", prefer);

        return headers;
    }
}
