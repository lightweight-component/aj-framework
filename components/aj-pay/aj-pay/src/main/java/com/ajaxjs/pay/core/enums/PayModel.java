package com.ajaxjs.pay.core.enums;

/**
 * 商户平台模式
 */
public enum PayModel {
    /**
     * 商户模式
     */
    BUSINESS_MODEL("BUSINESS_MODEL"),
    /**
     * 服务商模式
     */
    SERVICE_MODE("SERVICE_MODE");

    PayModel(String payModel) {
        this.payModel = payModel;
    }

    /**
     * 商户模式
     */
    private final String payModel;

    public String getPayModel() {
        return payModel;
    }
}
