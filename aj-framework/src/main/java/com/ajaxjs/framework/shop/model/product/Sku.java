package com.ajaxjs.framework.shop.model.product;

import com.ajaxjs.framework.model.BaseModelV2;
import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SKU (Stock Keeping Unit)
 * 库存量单元，具体的一个可售商品实例（如 iPhone 15 Pro 128GB 黑色）。每个 SKU 有唯一标识
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("shop_sku")
public class Sku extends BaseModelV2 {
    /**
     * 关联的 SPU ID
     */
    private Long spuId;

    /**
     * SKU 名称 (e.g., "iPhone 15 Pro 128GB Black")
     */
    private String name;

    /**
     * SKU 编码 (e.g., "IP15P-BLK-128")
     */
    private String code;

    /**
     * 销售价（单位：分）
     */
    private Integer price;

    /**
     * 成本价（单位：分）
     */
    private Integer costPrice;

    /**
     * 划线价/原价（单位：分）
     */
    private Integer marketPrice;

    /**
     * 总库存数量
     */
    private Integer stock;

    /**
     * 可售库存数量
     */
    private Integer saleStock;

    /**
     * 锁定库存数量
     */
    private Integer lockStock;

    /**
     * 重量 (kg)
     */
    private Integer weight;

    /**
     * 体积 (长x宽x高 cm)
     */
    private String volume;

    /**
     * SKU 图片URL
     */
    private String imageUrl;

    /**
     * 条形码/EAN码
     */
    private String barCode;
}
