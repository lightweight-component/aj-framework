package com.ajaxjs.business.banned_words;

import lombok.Data;

@Data
public class FilteredResult {
    /**
     * 文本最终警告级别
     */
    private Double level;

    /**
     * 屏蔽敏感词后的文本内容
     */
    private String filteredContent;

    /**
     * 屏蔽的敏感词串,例如:
     * - 色魔
     * - 法轮功
     * - GCD
     */
    private String badWords;

    /**
     * 检索到的正向单词
     */
    private String goodWords;

    /**
     * 原始文本内容
     */
    private String originalContent;

    /**
     * 是否包含敏感词
     */
    private Boolean hasSensitiveWords = false;
}
