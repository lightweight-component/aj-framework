package com.ajaxjs.rag.chunk;

import java.util.ArrayList;
import java.util.List;

public class FixedSizeSplitter implements TextSplitter {
    private final int size;

    public FixedSizeSplitter(int size) {
        this.size = size;
    }

    @Override
    public List<String> split(String text) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < text.length(); i += size) {
            int end = Math.min(text.length(), i + size);
            result.add(text.substring(i, end));
        }

        return result;
    }
}