package com.ajaxjs.rag.chunk;

import java.util.List;

public interface TextSplitter {
    List<String> split(String text);
}