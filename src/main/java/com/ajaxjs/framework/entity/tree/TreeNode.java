package com.ajaxjs.framework.entity.tree;

import com.ajaxjs.framework.BaseModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 树节点
 *
 * @param <T> id 类型
 */
@Data
public class TreeNode<T extends Serializable> extends BaseModel {
    /**
     * @param id
     * @param name
     * @param parentId
     */
    public TreeNode(T id, String name, T parentId) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
    }

    /**
     * 节点 id
     */
    private T id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点父亲 id
     */
    private T parentId;

    /**
     * 子节点
     */
    private List<TreeNode<T>> children;

    /**
     * 是否叶子
     */
    private Boolean isLeaf;
}
