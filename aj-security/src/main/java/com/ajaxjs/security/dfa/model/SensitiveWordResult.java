package com.ajaxjs.security.dfa.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 敏感词检测结果
 */
@Data
@AllArgsConstructor
public class SensitiveWordResult {
    /**
     * 敏感词内容
     */
    private String word;

    /**
     * 起始位置
     */
    private int start;

    /**
     * 结束位置
     */
    private int end;
}