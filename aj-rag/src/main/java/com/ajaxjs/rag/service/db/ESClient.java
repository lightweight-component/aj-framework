package com.ajaxjs.rag.service.db;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;
import com.ajaxjs.rag.constant.Config;
import com.ajaxjs.rag.entity.Document;
import com.ajaxjs.rag.utils.HttpClientUtil;
import com.ajaxjs.rag.utils.SnowflakeIdGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ESClient {
    private String esUrl;
    private String username;
    private String password;
    private OkHttpClient client;
    private static final String QUERY_TEMPLATE_PATH = "assert/es_search_chunk.sql";
    private static final ESClient instance = new ESClient(Config.esUrl, Config.esUserName, Config.esPassWord);

    public ESClient(String esUrl, String username, String password) {
        this.esUrl = esUrl;
        this.username = username;
        this.password = password;
        this.client = HttpClientUtil.createHttpClient(username, password);
    }

    public static ESClient getInstance() {
        return instance;
    }

    public void testConnection() {
        Request request = new Request.Builder()
                .url(esUrl + "/_cat/health")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("连接到Elasticsearch成功！");
            } else {
                System.out.println("连接到Elasticsearch失败，状态码：" + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 向Elasticsearch添加一个新的文档块。
     *
     * @param document 要添加的文档对象
     * @return 是否成功添加
     */
    public boolean addChunk(Document document) {
        // 生成唯一的ID
        String uniqueId = SnowflakeIdGenerator.generateUniqueID(); // 使用Snowflake算法生成唯一ID

        // 将Document对象转换为JSON格式的字符串
        String jsonString = JSON.toJSONString(document);

        // 发送POST请求到Elasticsearch
        RequestBody body = RequestBody.create(jsonString, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(esUrl + "/documents/_create/" + uniqueId)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            // 检查响应状态码是否为201，表示文档已成功添加
            if (response.isSuccessful() && response.code() == 201) {
                System.out.println("embedding 添加成功！");
                return true;
            } else {
                System.out.println("embedding 添加失败，状态码：" + response.code());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 从文件中读取查询模板并返回格式化的查询字符串。
     *
     * @param chunkText 文本内容
     * @param boost     提升值
     * @param userId    用户ID
     * @param size      结果大小
     * @return 格式化的查询字符串
     */
    private String getFormattedQuery(String chunkText, float boost, String userId, int size) {
        StringBuilder queryTemplate = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(QUERY_TEMPLATE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                queryTemplate.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return String.format(queryTemplate.toString(), chunkText, boost, userId, size);
    }

    public List<Document> searchChunk(Document document, float boost, int size) {
        String queryJson = getFormattedQuery(document.getChunkText(), boost, document.getUserId(), size);

        if (queryJson == null) {
            System.out.println("Failed to read query template.");
            return new ArrayList<>();
        }

        RequestBody body = RequestBody.create(queryJson, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(esUrl + "/documents/_search")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                return parseSearchResults(responseBody);
            } else {
                System.out.println("搜索失败，状态码：" + response.code());
                return new ArrayList<>();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Document> parseSearchResults(String responseBody) {
        List<Document> documents = new ArrayList<>();
        JSONObject responseJson = JSON.parseObject(responseBody);

        // 获取hits数组
        JSONObject hits = responseJson.getJSONObject("hits");
        JSONArray hitsArray = hits.getJSONArray("hits");

        for (int i = 0; i < hitsArray.size(); i++) {
            JSONObject hit = hitsArray.getJSONObject(i);
            JSONObject source = hit.getJSONObject("_source");

            // 直接将_source中的JSON反序列化为Document对象
            Document document = JSON.toJavaObject(source, Document.class);
            document.setScore(hit.getFloat("_score"));
            documents.add(document);
        }

        return documents;
    }


    public static void main(String[] args) {
        ESClient esClient = ESClient.getInstance();
        Document d = new Document();
        d.setUserId("200");
        d.setChunkText("哈利波特");
        esClient.addChunk(d);
        Document d_s = new Document();
        d_s.setUserId("200");
        d_s.setChunkText("哈利波特");

        List<Document> documents = esClient.searchChunk(d_s, 1, 10);
        System.out.println(documents);

    }
}