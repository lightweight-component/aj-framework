package com.ajaxjs.framework.shop.model.order;

import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;

/**
 * 订单明细项
 * 一个订单购买的多件不同的商品
 */
@Data
@Table("shop_order_item")
public class OrderItemEntity {
}
