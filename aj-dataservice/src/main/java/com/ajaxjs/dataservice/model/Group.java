package com.ajaxjs.dataservice.model;

import lombok.Data;

/**
 * A group has many endpoints.
 */
@Data
public class Group {
    /**
     * 分组的唯一标识。
     */
    Integer id;

    /**
     * 分组对应的 URL 前缀。
     */
    String url;

    /**
     * 用于展示的分组名称。
     */
    String name;
}
