package com.ajaxjs.framework.shop.model;

import com.ajaxjs.framework.model.BaseModelV2;
import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 交易流水 (shop_payment_transaction)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("shop_payment_transaction")
public class ShopRefundRecordEntity extends BaseModelV2 {
    /**
     * 交易系统订单号【对于三方来说：商户订单】
     */
    private Integer tradingOrderNo;

    /**
     * 业务系统订单号
     */
    private Integer productOrderNo;

    /**
     * 本次退款订单号
     */
    private Integer refundNo;

    /**
     * 第三方支付的退款单号
     */
    private String refundId;

    /**
     * 商户号
     */
    private Integer enterpriseId;

    /**
     * 退款渠道【支付宝、微信、现金】
     */
    private String tradingChannel;

    /**
     * 退款状态：0-发起退款,1-退款中，2-成功, 3-失败
     */
    private Integer refundStatus;

    /**
     * 返回编码
     */
    private String refundCode;

    /**
     * 返回信息
     */
    private String refundMsg;

    /**
     * 备注【订单门店，桌台信息】
     */
    private String memo;

    /**
     * 本次退款金额
     */
    private Long refundAmount;

    /**
     * 原订单金额
     */
    private Long total;
}
