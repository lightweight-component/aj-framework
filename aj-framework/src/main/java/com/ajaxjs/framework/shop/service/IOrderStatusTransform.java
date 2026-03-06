package com.ajaxjs.framework.shop.service;

public interface IOrderStatusTransform {
    /**
     * 订单状态转换
     *
     * @param orderStatus
     * @return
     */
    boolean canChange(OrderStatusTransform orderStatus);

    /**
     * 订单状态打印
     */
    void printOrderStatus();
}
