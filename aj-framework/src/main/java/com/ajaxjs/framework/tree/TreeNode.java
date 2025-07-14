package com.ajaxjs.framework.tree;

/**
 * 一个树节点
 */
public interface TreeNode {
    default void setId(Long id) {
    }

    default Long getId() {
        return null;
    }

    default Long getParentId() {
        throw new RuntimeException();
    }

    /**
     * 设置是否叶子
     *
     * @param isLeaf 是否叶子
     */
    default void setIsLeaf(Boolean isLeaf) {
        throw new RuntimeException();
    }

    default Boolean isLeaf() {
        throw new RuntimeException();
    }
}
