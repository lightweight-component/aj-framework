package com.ajaxjs.framework.shop.service;

import com.ajaxjs.framework.model.BusinessException;
import com.ajaxjs.framework.shop.model.Sku;
import com.ajaxjs.framework.shop.model.SkuAttributeValue;
import com.ajaxjs.framework.shop.model.Spu;
import com.ajaxjs.framework.shop.model.product.ProductDetailDTO;
import com.ajaxjs.framework.shop.model.product.ProductListDTO;
import com.ajaxjs.sqlman.Action;

import java.util.List;

public class ProductService {
    public ProductListDTO getProductList() {

        return null;
    }

    public ProductDetailDTO getProductDetail(Long spuId) {
        ProductDetailDTO dto = new ProductDetailDTO();

        Spu spu = new Action("SELECT * FROM shop_spu WHERE id = ?").query(spuId).one(Spu.class);

        if (spu == null)
            throw new BusinessException("SPU with id " + spuId + " not found.");

        dto.setSpu(spu);

        List<Sku> skus = new Action("SELECT * FROM shop_sku WHERE stat = 0 AND spu_id = ? ORDER BY id").query(spuId).list(Sku.class);
        dto.setSkus(skus);

        // 3. 查询这些 SKU 的所有属性值关联信息
        StringBuilder skuIdsInClause = new StringBuilder();

        for (int i = 0; i < skus.size(); i++) {
            if (i > 0)
                skuIdsInClause.append(",");

            skuIdsInClause.append("?");
        }

        String attrValueSql = "SELECT * FROM shop_sku_attribute_value WHERE sku_id IN (" + skuIdsInClause + ") ORDER BY sku_id, id"; // 按 SKU ID 和属性 ID 排序
        Object[] attrValueParams = skus.stream().mapToLong(Sku::getId).boxed().toArray();
        List<SkuAttributeValue> attrValues;

        return dto;
    }
}
