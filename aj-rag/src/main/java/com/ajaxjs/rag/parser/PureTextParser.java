package com.ajaxjs.rag.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PureTextParser implements FileParser{

    /**
     * 解析纯文本文件并返回其文本内容
     *
     * @param file 纯文本文件
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public  String parse(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            return text.toString();
        }
    }

    /**
     * 根据文件路径解析纯文本文件并返回其文本内容
     *
     * @param filePath 纯文本文件的路径
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public  String parse(String filePath) throws IOException {
        File file = new File(filePath);
        return parse(file);
    }

    // 测试方法
    public static void main(String[] args) {

    }
}