package com.ajaxjs.rag.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;

public class HTMLParser implements FileParser {

    /**
     * 解析HTML文件并返回其文本内容
     *
     * @param file HTML文件
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public  String parse(File file) throws IOException {
        Document doc = Jsoup.parse(file, "UTF-8");
        return doc.text();
    }

    /**
     * 根据文件路径解析HTML文件并返回其文本内容
     *
     * @param filePath HTML文件的路径
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public  String parse(String filePath) throws IOException {
        File file = new File(filePath);
        return parse(file);
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

    // 测试方法
    public static void main(String[] args) {
        try {
            HTMLParser htmlParser = new HTMLParser();

            // 解析本地HTML文件
            String localContent = htmlParser.parse("path/to/your/html_file.html");
            System.out.println(localContent);

            // 解析在线HTML页面
            String onlineContent = parseHTMLFromURL("https://example.com");
            System.out.println(onlineContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}