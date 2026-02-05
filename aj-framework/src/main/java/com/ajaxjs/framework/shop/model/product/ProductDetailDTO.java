package com.ajaxjs.framework.shop.model.product;

import com.ajaxjs.framework.shop.model.Sku;
import com.ajaxjs.framework.shop.model.Spu;
import lombok.Data;

import java.util.List;

/**
 * 商品详情
 */
@Data
public class ProductDetailDTO {
    private Spu spu;

    /**
     * 该 SPU 下的所有 SKU
     */
    private List<Sku> skus;
}
