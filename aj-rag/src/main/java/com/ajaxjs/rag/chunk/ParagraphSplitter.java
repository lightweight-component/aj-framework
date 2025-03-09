package com.ajaxjs.rag.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParagraphSplitter implements TextSplitter {
    /**
     * 实现 split 方法，用于将文本按段落分割
     *
     * @param text
     * @return
     */
    public List<String> split(String text) {
        String[] paragraphs = text.split("\\n\\n");// 以换行符作为段落分隔符，将文本分割成段落数组

        return new ArrayList<>(Arrays.asList(paragraphs));// 将段落数组转换为 List 并返回
    }
}