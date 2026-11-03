package com.ajaxjs.security.dfa.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 过滤结果封装类
 */
@Getter
@RequiredArgsConstructor
public class FilterResult {
    /**
     * 原始文本
     */
    private final String originalText;

    /**
     * 过滤后文本
     */
    private final String filteredText;

    /**
     * 是否包含敏感词
     */
    private final boolean hasSensitive;

    /**
     * 敏感词列表
     */
    private final List<SensitiveWordResult> sensitiveWords;

    /**
     * 敏感词数量
     */
    private final int sensitiveWordCount;
}