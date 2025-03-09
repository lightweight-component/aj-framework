package com.ajaxjs.rag.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PureTextParser extends BaseFileParser {
    /**
     * 解析纯文本文件并返回其文本内容
     *
     * @param file 纯文本文件
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public String parse(File file) throws IOException {
        StringBuilder text = new StringBuilder();
        String line;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((line = reader.readLine()) != null)
                text.append(line).append("\n");

            return text.toString();
        }
    }
}