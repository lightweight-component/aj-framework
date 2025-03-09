package com.ajaxjs.rag.parser;

import java.io.File;
import java.io.IOException;

public interface FileParser {
    public String parse(File file) throws IOException;
    public String parse(String filePath) throws IOException;
}