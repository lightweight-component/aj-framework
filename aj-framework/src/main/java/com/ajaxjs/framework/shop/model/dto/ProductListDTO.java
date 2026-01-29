package com.ajaxjs.framework.shop.model.dto;

import lombok.Data;

@Data
public class ProductListDTO {
    /**
     * 商品 id
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 品牌 id
     */
    private Integer brandId;

    /**
     * 分类 id
     */
    private Integer categoryId;

    /**
     * 分类
     */
    private String categoryName;

    /**
     * 图片
     */
    private String mainImageUrl;

    /**
     * SPU下所有SKU的最低价格
     */
    private Long minPrice;
}
