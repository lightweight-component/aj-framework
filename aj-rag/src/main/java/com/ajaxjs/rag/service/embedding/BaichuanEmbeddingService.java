package com.ajaxjs.rag.service.embedding;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class BaichuanEmbeddingService implements EmbeddingService {
    private final String apiKey;
    private final OkHttpClient client;

    public BaichuanEmbeddingService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    /**
     * 发送请求到指定的Embeddings API并获取响应
     *
     * @param url   API的URL
     * @param input 输入文本
     * @return 嵌入向量
     * @throws IOException 如果请求失败
     */
    public double[] getEmbedding(String url, String input) throws IOException {
        RequestBody body = RequestBody.create(
                new JSONObject()
                        .put("model", "Baichuan-Text-Embedding")
                        .put("input", input)
                        .toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            assert response.body() != null;
            String responseBody = response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONObject dataObject = jsonObject.getJSONArray("data").getJSONObject(0);
            double[] embedding = new double[dataObject.getJSONArray("embedding").length()];
            for (int i = 0; i < embedding.length; i++) {
                embedding[i] = dataObject.getJSONArray("embedding").getDouble(i);
            }
            return embedding;
        }
    }

    public double[][] getEmbeddings(String url, String[] inputs) throws IOException {
        JSONArray inputArray = new JSONArray();
        for (String input : inputs)
            inputArray.put(input);

        RequestBody body = RequestBody.create(
                new JSONObject()
                        .put("model", "Baichuan-Text-Embedding")
                        .put("inputs", inputArray)
                        .toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            JSONArray dataArray = jsonObject.getJSONArray("data");
            double[][] embeddings = new double[dataArray.length()][];

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                JSONArray embeddingArray = dataObject.getJSONArray("embedding");
                double[] embedding = new double[embeddingArray.length()];
                for (int j = 0; j < embeddingArray.length(); j++) {
                    embedding[j] = embeddingArray.getDouble(j);
                }
                embeddings[i] = embedding;
            }
            return embeddings;
        }
    }
}