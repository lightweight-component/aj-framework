package com.ajaxjs.framework.shop.model;

import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 属性值 - 如 "红色", "XL"
 */
@Data
@Table("shop_attribute_value")
public class AttributeValue {
    /**
     * 主键 id，自增
     */
    private Long id;

    /**
     * 关联的属性定义 ID
     */
    private Long attrDefId;

    /**
     * 属性值，如 "红色", "XL"
     */
    private String value;

    /**
     * 简介
     */
    private String content;

    /**
     * 创建人名称（可冗余的）
     */
    private String creator;

    /**
     * 创建人 id
     */
    private Long creatorId;

    /**
     * 创建日期
     */
    private LocalDateTime createDate;
}
