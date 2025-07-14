package com.ajaxjs.framework.tree;

import lombok.Data;

import java.io.Serializable;

/**
 * 基础树的结构
 */
@Data
public abstract class BaseTreeStrut {
    private String idField = "id";

    private String parentIdField = "parentId";

    private String childrenField = "children";

    private Serializable topNodeValue = -1;
}
