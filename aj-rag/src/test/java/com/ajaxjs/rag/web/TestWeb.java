package com.ajaxjs.rag.web;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestWeb {
    @Test
    public void testKeywordToMarkdownCrawler() {
        long startTime = System.currentTimeMillis(); // 记录程序开始时间
        String keyword = "哪吒二"; // 示例关键词
        List<String> markdowns = KeywordToMarkdownCrawler.getMarkdownsForKeyword(keyword);

        // 打印获取到的 Markdown 内容
        for (String markdown : markdowns) {
            System.out.println(markdown);
        }
        long endTime = System.currentTimeMillis(); // 记录程序结束时间
        long duration = (endTime - startTime) / 1000; // 计算运行时间（秒）
        System.out.println(markdowns.size());
        System.out.println("程序运行时间为：" + duration + " 秒");
    }

    @Test
    public void testSearchEngine() {        // 构建请求参数
        Map<String, String> parameter = new HashMap<>();
        parameter.put("engine", "bing");
        parameter.put("q", "哪吒二");
        parameter.put("api_key", "1af00627e582c9238b8c947d2300dd13331a9817523811a83dc16245ed98d444");
        JsonObject results = SearchEngine.getResult(parameter);

        if (results != null) {
            SearchEngine.parseResults(results);
            // 调用 parseResultsHtml 方法获取 HTML 表格字符串
            String htmlTable = SearchEngine.parseResultsHtml(results);
            System.out.println(htmlTable);

        }
    }

    @Test
    public void testUrlToMarkdownConverter() {
        long startTime = System.currentTimeMillis(); // 记录程序开始时间
        String url = "https://zh.wikipedia.org/wiki/哪吒之魔童闹海";

        try {
            String markdown = UrlToMarkdownConverter.convertUrlToMarkdown(url);
            System.out.println(markdown);
        } catch (IOException e) {
            System.err.println("获取网页内容时出现错误: " + e.getMessage());
        }
        long endTime = System.currentTimeMillis(); // 记录程序结束时间
        long duration = (endTime - startTime) / 1000; // 计算运行时间（秒）
        System.out.println("程序运行时间为：" + duration + " 秒");
    }
}
