package com.ajaxjs.web.website;

import lombok.Data;

import java.util.List;

/**
 * 页面节点
 */
@Data
public class WebPageNode {
    /**
     * 节点 URL 路径（不包含前缀）
     */
    private String path;

    /**
     * 名称
     */
    private String name;

    /**
     * 子节点列表
     */
    private List<WebPageNode> children;
}
