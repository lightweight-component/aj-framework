package com.ajaxjs.rag.parser;

import java.io.File;
import java.io.IOException;

public interface FileParser {
    String parse(File file) throws IOException;

    String parse(String filePath) throws IOException;
}