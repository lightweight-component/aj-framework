package com.ajaxjs.pay.paypal;

/**
 * PayPal 支付接口 URL
 */
public interface PayPalApiUrl {
    /**
     * 沙箱环境
     */
    String SANDBOX_GATEWAY = "https://api.sandbox.paypal.com";

    /**
     * 线上环境
     */
    String LIVE_GATEWAY = "https://api.paypal.com";

    /**
     * 获取 Access Token
     */
    String GET_TOKEN = "/v1/oauth2/token";

    /**
     * 订单
     */
    String CHECKOUT_ORDERS = "/v2/checkout/orders";

    /**
     * 确认订单
     */
    String CAPTURE_ORDER = "/v2/checkout/orders/%s/capture";

    /**
     * 查询已确认订单
     */
    String CAPTURE_QUERY = "/v2/payments/captures/%s";

    /**
     * 退款
     */
    String REFUND = "/v2/payments/captures/%s/refund";

    /**
     * 退款查询
     */
    String REFUND_QUERY = "/v2/payments/refunds/%s";
}