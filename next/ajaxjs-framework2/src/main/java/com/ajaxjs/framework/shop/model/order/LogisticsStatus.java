package com.ajaxjs.framework.shop.model.order;

/**
 * 物流状态枚举
 */
public
enum LogisticsStatus {
    /**
     * 待发货
     */
    AWAITING_SHIPMENT,

    /**
     * 已发货
     */
    SHIPPED,

    /**
     * 运输中
     */
    IN_TRANSIT,

    /**
     * 派送中
     */
    OUT_FOR_DELIVERY,

    /**
     * 已签收
     */
    DELIVERED,

    /**
     * 投递失败
     */
    FAILED_TO_DELIVER
}