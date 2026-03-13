package com.ajaxjs.framework.shop.model.product;

import com.ajaxjs.sqlman.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 属性定义 - 如 "颜色", "尺寸"
 */
@Data
@Table("shop_attribute_definition")
public class AttributeDefinition {
    /**
     * 主键 id，自增
     */
    private Long id;

    /**
     * 属性名称，如 "颜色", "尺寸"
     */
    private String name;

    /**
     * 简介
     */
    private String content;

    /**
     * 所属分类 id
     */
    private Long categoryId;

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
