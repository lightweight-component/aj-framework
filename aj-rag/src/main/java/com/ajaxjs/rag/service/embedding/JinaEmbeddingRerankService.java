package com.ajaxjs.rag.service.embedding;

import com.ajaxjs.rag.constant.Config;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class JinaEmbeddingRerankService implements EmbeddingService {
    private final String apiKey;
    private final OkHttpClient client;

    public static final JinaEmbeddingRerankService instance = new JinaEmbeddingRerankService(Config.Jina_API_KEY);

    public static JinaEmbeddingRerankService getInstance() {
        return instance;
    }

    public JinaEmbeddingRerankService(String apiKey) {
        this.apiKey = Config.Jina_API_KEY;
        this.client = new OkHttpClient();
    }

    public JSONArray getMultiVectorEmbeddingJSONArray(String url, String[] inputs) throws IOException {
        JSONArray inputArray = new JSONArray();
        for (String input : inputs) {
            inputArray.put(input);
        }

        JSONObject requestBody = new JSONObject()
                .put("model", "jina-colbert-v2")
                .put("dimensions", 128)
                .put("input_type", "query")
                .put("embedding_type", "float")
                .put("input", inputArray);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
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
            return jsonObject.getJSONArray("data");
        }
    }

    public double[][] getEmbeddings(String url, String[] inputs) throws IOException {
        JSONArray data = this.getMultiVectorEmbeddingJSONArray(url, inputs);
        int vectorCount = data.length();  // 获取嵌入向量的数量
        // 获取第一个嵌入向量的维度
        int vectorDimension = data.getJSONObject(0).getJSONArray("embeddings").getJSONArray(0).length();
        double[][] embeddings = new double[vectorCount][vectorDimension];  // 创建二维 double 数组来存储嵌入向量

        for (int i = 0; i < vectorCount; i++) {
            // 注意这里需要获取 "embeddings" 数组的第一个元素，它才是实际的嵌入向量
            JSONArray embeddingArray = data.getJSONObject(i).getJSONArray("embeddings").getJSONArray(0);
            for (int j = 0; j < vectorDimension; j++)
                embeddings[i][j] = embeddingArray.getDouble(j);
        }

        return embeddings;
    }

    public double[] getEmbedding(String url, String input) throws IOException {
        String[] inputs = {input};  // 将单个输入放入数组中
        double[][] embeddingsArray = getEmbeddings(url, inputs);  // 调用原有的getEmbeddings方法获取嵌入向量数组

        return embeddingsArray[0];  // 返回第一个（也是唯一一个）嵌入向量
    }
}