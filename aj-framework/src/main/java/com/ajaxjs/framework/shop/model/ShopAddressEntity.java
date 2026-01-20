package com.ajaxjs.framework.shop.model;

import com.ajaxjs.framework.model.BaseModelV2;
import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收货地址 (shop_address)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("shop_address")
public class ShopAddressEntity extends BaseModelV2 {
    /**
     * 说明
     */
    private String name;

    /**
     * 地址
     */
    private String address;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 收货人
     */
    private String receiver;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 是否默认收货地址
     */
    private Boolean defaultAddress;
}
