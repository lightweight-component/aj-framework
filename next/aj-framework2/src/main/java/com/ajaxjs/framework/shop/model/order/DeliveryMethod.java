package com.ajaxjs.framework.shop.model.order;

/**
 * 配送方式枚举
 */
public enum DeliveryMethod {
    /**
     * 快递/物流
     */
    EXPRESS,

    /**
     * 同城
     */
    COURIER,

    /**
     * 门店自提
     */
    PICKUP_IN_STORE,

    /**
     * 其他
     */
    OTHER
}