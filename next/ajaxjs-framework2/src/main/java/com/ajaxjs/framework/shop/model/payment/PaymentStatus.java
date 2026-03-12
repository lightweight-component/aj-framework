package com.ajaxjs.framework.shop.model.payment;

/**
 * 支付状态枚举
 */
public enum PaymentStatus {
    /**
     * 未支付
     */
    NOT_PAID,

    /**
     * 支付中
     */
    PAYING,

    /**
     * 已支付
     */
    PAID,

    /**
     * 支付失败
     */
    FAILED,

    /**
     * 已退款
     */
    REFUNDED
}
