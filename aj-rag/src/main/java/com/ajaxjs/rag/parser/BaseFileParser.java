package com.ajaxjs.rag.parser;

import java.io.File;
import java.io.IOException;

public abstract class BaseFileParser implements FileParser {
    @Override
    public String parse(String filePath) throws IOException {
        File file = new File(filePath);
        return parse(file);
    }
}
