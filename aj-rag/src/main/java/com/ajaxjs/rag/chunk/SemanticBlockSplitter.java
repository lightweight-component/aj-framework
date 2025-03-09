package com.ajaxjs.rag.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SemanticBlockSplitter implements TextSplitter {
    // todo 这里暂时简化处理，假设每个段落都是一个语义块
    @Override
    public List<String> split(String text) {
        String[] blocks = text.trim().split("\\n\\n");
        return new ArrayList<>(Arrays.asList(blocks));
    }
}