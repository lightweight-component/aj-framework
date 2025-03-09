package com.ajaxjs.rag.chunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SentenceSplitter implements TextSplitter {
    private static final Pattern SENTENCE_DELIMITER = Pattern.compile("[.!?] +");

    @Override
    public List<String> split(String text) {
        String[] sentences = SENTENCE_DELIMITER.split(text.trim());
        return new ArrayList<>(Arrays.asList(sentences));
    }
}