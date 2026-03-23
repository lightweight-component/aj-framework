package com.ajaxjs.framework.shop.model.order;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    /**
     * 待支付
     */
    PENDING_PAYMENT,

    /**
     * 已确认（已支付）
     */
    CONFIRMED,

    /**
     * 已发货
     */
    SHIPPED,

    /**
     * 已送达（物流节点）
     */
    DELIVERED,

    /**
     * 已完成（交易完成）
     */
    COMPLETED,

    /**
     * 已取消
     */
    CANCELLED,

    /**
     * 已退款
     */
    REFUNDED
}