package com.ajaxjs.rag.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

public class HTMLParser extends BaseFileParser {
    /**
     * 解析HTML文件并返回其文本内容
     *
     * @param file HTML文件
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    @Override
    public String parse(File file) throws IOException {
        Document doc = Jsoup.parse(file, "UTF-8");
        return doc.text();
    }

    /**
     * 解析URL指向的HTML页面并返回其文本内容
     *
     * @param url URL字符串
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public static String parseHTMLFromURL(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();

        return doc.text();
    }
}