package com.ajaxjs.framework.shop.model;

import com.ajaxjs.framework.model.BaseModelV2;
import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("shop_product")
public class ShopProduct extends BaseModelV2 {
    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品型号
     */
    private String marque;

    /**
     * 仓库条码
     */
    private String barcode;

    /**
     * 类型编号
     */
    private Integer typeId;

    /**
     * 类别编号
     */
    private Integer categoryId;

    /**
     * 品牌编号
     */
    private Integer brandId;

    /**
     * 商品价格
     */
    private Long price;

    /**
     * 市场价格
     */
    private Long marketPrice;

    /**
     * 成本价格（进货价格）
     */
    private Long costPrice;

    /**
     * 库存量
     */
    private Integer stock;

    /**
     * 告警库存
     */
    private Integer warningStock;

    /**
     * 商品积分
     */
    private Integer integral;

    /**
     * 商品图片URL
     */
    private String pictureUrl;
}
