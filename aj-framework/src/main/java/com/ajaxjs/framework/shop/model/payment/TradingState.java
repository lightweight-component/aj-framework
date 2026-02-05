package com.ajaxjs.framework.shop.model.payment;

/**
 * 交易状态枚举
 * 对应 shop_payment_transaction 表中的 trading_state 字段
 */
public enum TradingState {
    /**
     * 待支付
     */
    PENDING_PAYMENT,

    /**
     * 支付中
     */
    PAYING,

    /**
     * 支付成功
     */
    SUCCESS,

    /**
     * 支付失败
     */
    FAILED,

    /**
     * 已关闭
     */
    CLOSED,

    /**
     * 退款中
     */
    REFUNDING,

    /**
     * 退款成功
     */
    REFUNDED_SUCCESS,

    /**
     * 退款失败
     */
    REFUNDED_FAILED
}