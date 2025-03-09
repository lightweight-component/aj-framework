package com.ajaxjs.rag.service.LLM;

import okhttp3.OkHttpClient;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;

public class OllamaChatService {
    private final OkHttpClient client = new OkHttpClient();

    public String generateChatCompletion(String model, String message) throws Exception {
        String url = "http://localhost:11434/api/chat";

        // 创建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        JSONObject messageObject = new JSONObject();
        messageObject.put("role", "user");
        messageObject.put("content", message);
        requestBody.put("messages", new JSONObject[]{messageObject});

        // 转换为JSON字符串
        String jsonRequestBody = requestBody.toString();
        RequestBody body = RequestBody.create(jsonRequestBody, MediaType.get("application/json; charset=utf-8"));

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // 执行请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            // 获取响应体
            assert response.body() != null;
            return response.body().string();
        }
    }

    public static void main(String[] args) {
        OllamaChatService service = new OllamaChatService();

        try {
            String response = service.generateChatCompletion("llama3.2", "why is the sky blue?");
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}