package com.ajaxjs.framework.shop.model.product;

import com.ajaxjs.framework.model.BaseModelV2;
import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SKU 与 属性值 的关联表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("shop_sku_attribute_value")
public class SkuAttributeValue extends BaseModelV2 {
    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 关联的属性定义 ID
     */
    private Long attrDefId;

    /**
     * 冗余存储属性定义名称，如 "名称"
     */
    private String attrDefName;

    /**
     * 关联的属性值 ID
     */
    private Long attrValueId;

    /**
     * 冗余存储属性值文本，如 "黑色"
     */
    private String attrValueText;
}
