package com.ajaxjs.pay.wechat;

/**
 * 支付方式
 */
public enum TradeType {
    /**
     * 微信公众号支付或者小程序支付
     */
    JSAPI,

    /**
     * 微信扫码支付
     */
    NATIVE,

    /**
     * 微信APP支付
     */
    APP,

    /**
     * 付款码支付
     */
    MICROPAY,

    /**
     * H5支付
     */
    MWEB;
}
