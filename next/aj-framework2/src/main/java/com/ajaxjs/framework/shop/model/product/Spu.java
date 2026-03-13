package com.ajaxjs.framework.shop.model.product;

import com.ajaxjs.framework.model.BaseModelV2;
import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SPU (Standard Product Unit)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("shop_spu")
public class Spu extends BaseModelV2 {
    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 所属分类ID
     */
    private Long categoryId;

    /**
     * SPU级别描述
     */
    private String description;

    /**
     * 商品标签
     */
    private String tags;

    /**
     * 商品编码
     */
    private String code;

    /**
     * 主图URL
     */
    private String mainImageUrl;
}
