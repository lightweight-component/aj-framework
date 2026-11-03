package com.ajaxjs.security.dfa.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Trie 树节点
 * DFA 算法的核心数据结构
 */
@Data
public class TrieNode {
    /**
     * 子节点映射：字符 -> Trie节点
     */
    private Map<Integer, TrieNode> children = new HashMap<>();

    /**
     * 是否为敏感词的结束节点
     */
    private boolean isEnd = false;

    /**
     * 完整敏感词内容（便于输出）
     */
    private String keyword;

    /**
     * 获取子节点
     */
    public TrieNode getChild(int c) {
        return children.get(c);
    }

    /**
     * 添加子节点
     */
    public TrieNode addChild(int c) {
        return children.computeIfAbsent(c, k -> new TrieNode());
    }

    /**
     * 是否包含指定字符的子节点
     */
    public boolean hasChild(int c) {
        return children.containsKey(c);
    }
}
