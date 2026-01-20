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
public class ShopPaymentTransactionEntity extends BaseModelV2 {
    /**
     * 业务系统订单号
     */
    private Long productOrderNo;
    /**
     * 交易系统订单号【对于三方来说：商户订单】
     */
    private Long tradingOrderNo;
    /**
     * 第三方支付交易号
     */
    private String transactionId;
    /**
     * 支付渠道
     */
    private String tradingChannel;
    /**
     * 交易类型【付款、退款】
     */
    private String tradingType;
    /**
     * 交易状态【2-支付成功，其他状态可以自定义】
     */
    private Integer tradingState;
    /**
     * 收款人姓名
     */
    private String payeeName;
    /**
     * 收款人账号ID
     */
    private Integer payeeId;
    /**
     * 付款人姓名
     */
    private String payerName;
    /**
     * 付款人ID
     */
    private Integer payerId;
    /**
     * 交易金额，单位：分
     */
    private Integer tradingAmount;
    /**
     * 退款金额，单位：分
     */
    private Integer refund;
    /**
     * 是否退款
     */
    private String isRefund;
    /**
     * 返回编码
     */
    private String resultCode;
    /**
     * 返回信息
     */
    private String resultMsg;
    /**
     * 第三方交易返回信息JSON
     */
    private String resultJson;
    /**
     * 统一下单返回编码
     */
    private String placeOrderCode;

    /**
     * 统一下单返回信息
     */
    private String placeOrderMsg;

    /**
     * 统一下单返回信息JSON
     */
    private String placeOrderJson;

    /**
     * 备注【订单门店，桌台信息】
     */
    private String memo;

    /**
     * 用户open_id
     */
    private String openId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 用户名
     */
    private String userName;
}
