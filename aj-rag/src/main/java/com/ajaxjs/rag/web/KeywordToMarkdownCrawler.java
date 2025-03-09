package com.ajaxjs.rag.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class KeywordToMarkdownCrawler {
    private static final int TIMEOUT_SECONDS = 1; // 设置超时时间

    public static List<String> getMarkdownsForKeyword(String keyword) {
        List<String> markdowns = new ArrayList<>();

        // 构建请求参数
        Map<String, String> parameter = new HashMap<>();
        parameter.put("engine", "bing");
        parameter.put("q", keyword);
        parameter.put("api_key", "1af00627e582c9238b8c947d2300dd13331a9817523811a83dc16245ed98d444"); // 替换为你的 API 密钥
        parameter.put("count", "50");
        // 获取搜索结果
        JsonObject results = SearchEngine.getResult(parameter);
        if (results != null) {
            // 解析搜索结果中的 URL
            JsonArray organicResults = results.getAsJsonArray("organic_results");
            if (organicResults != null) {
                ExecutorService executor = Executors.newFixedThreadPool(100); // 创建固定线程池
                List<Future<String>> futures = new ArrayList<>();

                for (int i = 0; i < organicResults.size(); i++) {
                    JsonObject result = organicResults.get(i).getAsJsonObject();
                    String link = result.get("link").getAsString();

                    // 创建 FutureTask
                    Callable<String> callable = () -> {
                        try {
                            return UrlToMarkdownConverter.convertUrlToMarkdown(link);
                        } catch (IOException e) {
                            throw new RuntimeException("获取 Markdown 内容时出现错误: " + e.getMessage(), e);
                        }
                    };

                    Future<String> future = executor.submit(callable);
                    futures.add(future);
                }

                for (Future<String> future : futures) {
                    try {
                        String markdown = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        markdowns.add(markdown);
                    } catch (TimeoutException e) {
                        future.cancel(true); // 取消超时的任务
                        System.out.println("任务超时并已取消: " + e.getMessage());
                    } catch (InterruptedException | ExecutionException e) {
                        System.out.println("任务执行过程中出现错误: " + e.getMessage());
                    }
                }

                executor.shutdown(); // 关闭线程池
            }
        }

        return markdowns;
    }
}