package com.ajaxjs.framework.entity.tree;

import lombok.Data;

import java.util.List;

@Data
public abstract class BaseTreeHandle<E> {
    private List<E> allNodes;

    private List<E> roots;

    /**
     * @param allNodes 节点列表，包含所有需要转换的数据
     */
    public BaseTreeHandle(List<E> allNodes) {
        this.allNodes = allNodes;
    }

    public BaseTreeHandle() {
    }

    private Integer topNodeValue = -1;

    abstract public void init();

    abstract public void setLeafFlag();

    abstract public void printTree();
}
