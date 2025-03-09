package com.ajaxjs.rag.controler;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;
import com.ajaxjs.rag.service.LLM.OpenAIChatService;
import com.ajaxjs.rag.constant.Config;
import com.ajaxjs.rag.web.SearchEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SearchController extends NanoHTTPD {

    public SearchController(int port) throws IOException {
        super(port);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Search Server started on port " + port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        try {
            // 添加 CORS 头信息，允许所有来源的请求
            Response response;

            if (Method.OPTIONS.equals(method)) {
                response = newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "");
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                response.addHeader("Access-Control-Allow-Headers", "Content-Type");
                return response;
            }

            if (Method.POST.equals(method) && "/search".equals(uri)) {
                // 处理 POST 请求，获取搜索关键词并调用 SearchEngine 进行搜索
                try {
                    session.parseBody(new HashMap<>());
                } catch (ResponseException e) {
                    throw new RuntimeException(e);
                }
                Map<String, String> params = session.getParms();
                String keyword = params.get("keyword");

                if (keyword != null && !keyword.isEmpty()) {
                    Map<String, String> searchParams = new HashMap<>();
                    searchParams.put("engine", "baidu");
                    searchParams.put("q", keyword);
                    searchParams.put("api_key", Config.SerpAPI);


                    JsonObject searchResult = SearchEngine.getResult(searchParams);
                    if (searchResult != null) {
                        // 可以在这里对搜索结果进行处理，比如转换为 HTML 格式返回给前端
                        StringBuilder html = new StringBuilder();
                        html.append(parseResultsHtml(searchResult));
                        response = newFixedLengthResponse(html.toString());
                    } else {
                        response = newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "搜索失败");
                    }
                } else {
                    response = newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "关键词不能为空");
                }
            } else {
                response = newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "未找到该页面");
            }

            response.addHeader("Access-Control-Allow-Origin", "*");
            return response;
        } catch (IOException | RuntimeException e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "服务器内部错误: " + e.getMessage());
        }
    }

    // 新增方法：将搜索结果转换为 HTML 内容
    private String parseResultsHtml(JsonObject results) {
        StringBuilder html = new StringBuilder();
        if (results != null) {
            // 解析 search_information 中的 query_displayed 字段
            JsonObject searchInformation = results.getAsJsonObject("search_information");
            if (searchInformation != null) {
                String queryDisplayed = searchInformation.get("query_displayed").getAsString();
                html.append("<p>查询关键词: ").append(queryDisplayed).append("</p>");
            }

            // 解析 organic_results 数组中的 title、link 和 snippet 字段
            JsonArray organicResults = results.getAsJsonArray("organic_results");
            if (organicResults != null) {
                for (int i = 0; i < organicResults.size(); i++) {
                    JsonObject result = organicResults.get(i).getAsJsonObject();
                    String title = result.get("title").getAsString();
                    String link = result.get("link").getAsString();
                    String snippet = "";
                    if (result.has("snippet") && !result.get("snippet").isJsonNull()) {
                        snippet = result.get("snippet").getAsString();
                    }
                    html.append("<li>");
                    html.append("<h3><a href='").append(link).append("' target='_blank'>").append(title).append("</a></h3>");
                    html.append("<p>").append(snippet).append("</p>");
                    html.append("</li>");
                }
            }
        }
        return html.toString();
    }

    public static void main(String[] args) {
        try {
            new SearchController(8080);
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }
}