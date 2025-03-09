package com.ajaxjs.rag.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecursiveSplitter implements TextSplitter {
    private final int depth;

    public RecursiveSplitter(int depth) {
        this.depth = depth;
    }

    @Override
    public List<String> split(String text) {
        return recursiveSplit(text, 0);
    }

    private List<String> recursiveSplit(String text, int currentDepth) {
        if (currentDepth >= depth)
            return new ArrayList<>(Arrays.asList(text));

        List<String> result = new ArrayList<>();

        int midIndex = text.length() / 2;
        String left = text.substring(0, midIndex);
        String right = text.substring(midIndex);

        result.addAll(recursiveSplit(left, currentDepth + 1));
        result.addAll(recursiveSplit(right, currentDepth + 1));

        return result;
    }
}