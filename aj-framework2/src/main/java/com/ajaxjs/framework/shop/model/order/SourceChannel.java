package com.ajaxjs.framework.shop.model.order;

/**
 * 订单来源渠道枚举
 */
public enum SourceChannel {
    /**
     * 苹果 App
     */
    APP_IOS,

    /**
     * 安卓 App
     */
    APP_ANDROID,

    /**
     * 微信小程序
     */
    WECHAT_MINI_PROGRAM,

    /**
     * H5 页面
     */
    H5,

    /**
     * PC 网站
     */
    PC_WEB,

    /**
     * 其他
     */
    OTHER
}