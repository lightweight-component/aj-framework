package com.ajaxjs.business.banned_words;

import lombok.Data;

/**
 * 找到到单词，涉及下标及敏感度（权重）
 */
@Data
public class Word {
    private int[] pos;

    private int startPos;

    private int endPos;

    private String word;

    /**
     * 重要级别
     */
    private double level;
}
