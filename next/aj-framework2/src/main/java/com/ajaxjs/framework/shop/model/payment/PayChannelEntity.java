package com.ajaxjs.framework.shop.model.payment;

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
public class PayChannelEntity extends BaseModelV2 {
    /**
     * 域名
     */
    private String domain;

    /**
     * 商户 appid
     */
    private String appId;

    /**
     * 支付公钥
     */
    private String publicKey;

    /**
     * 商户私钥
     */
    private String merchantPrivateKey;

    /**
     * 其他配置
     */
    private String otherConfig;

    /**
     * AES混淆密钥
     */
    private String encryptKey;

    /**
     * 说明
     */
    private String remark;

    /**
     * 回调地址
     */
    private String notifyUrl;
}
