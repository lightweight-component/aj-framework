package com.ajaxjs.rag.service.LLM;

import okhttp3.*;
import com.ajaxjs.rag.constant.Config;
import org.json.JSONObject;
import org.service.db.RedisClient;

import java.io.IOException;
import java.util.List;

public class OpenAIChatService {

    private final String apiKey;
    private final OkHttpClient client;

    public OpenAIChatService(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    /**
     * 发送请求到指定的API并获取响应
     *
     * @param url    API的URL
     * @param params 请求参数
     * @return 生成的文本
     * @throws IOException 如果请求失败
     */
    public String generateText(String url, JSONObject params) throws IOException {
        RequestBody body = RequestBody.create(params.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful())
                throw new IOException("Unexpected code " + response);

            assert response.body() != null;
            String responseBody = response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            return jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
        }
    }


    /**
     * 发送请求到指定的API并获取响应
     *
     * @param url        API的URL
     * @param chatId     对话的唯一标识
     * @param newMessage 新的消息内容
     * @return 生成的文本
     * @throws IOException 如果请求失败
     */
    public String generateText(String url, String chatId, JSONObject newMessage) throws IOException {
        RedisClient redisClient = RedisClient.getInstance();
        // 从 Redis 中获取现有的 messages 历史记录
        List<String> historyMessages = RedisClient.lrange(chatId, 0, -1);
        JSONObject[] messageArray = new JSONObject[historyMessages.size() + 1];
        for (int i = 0; i < historyMessages.size(); i++)
            messageArray[i] = new JSONObject(historyMessages.get(i));

        messageArray[messageArray.length - 1] = newMessage;

        // 构建请求参数
        JSONObject params = new JSONObject()
                .put("model", Config.LLM_MODEL)
                .put("messages", messageArray)
                .put("temperature", 0.3)
                .put("stream", false);

        // 发送请求并获取响应
        RequestBody body = RequestBody.create(params.toString(), MediaType.get("application/json; charset=utf-8"));
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
            String generatedText = jsonObject.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");

            // 将新的消息和生成的文本添加到 messages 历史记录中
            RedisClient.lpush(chatId, newMessage.toString(), Config.REDIS_EXPIRE_SECONDS);
            RedisClient.lpush(chatId, new JSONObject().put("role", "assistant").put("content", generatedText).toString(), Config.REDIS_EXPIRE_SECONDS);

            return generatedText;
        }
    }


    public static void main(String[] args) {
        // 替换为您的API密钥
        String apiKey = Config.API_KEY;
        // 使用百川Baichuan3-Turbo模型
        String model = Config.LLM_MODEL;
        // API的URL
        String url = Config.LLM_URL;

        OpenAIChatService openAIChatService = new OpenAIChatService(apiKey);

        try {
            // 构建请求参数
            JSONObject params = new JSONObject()
                    .put("model", model)
                    .put("messages", new JSONObject[]{
                            new JSONObject().put("role", "user").put("content", "1+1 = ?")
                    })
                    .put("temperature", 0.3)
                    .put("stream", false);

            // 这里可以替换为您想要询问的问题
            String generatedText = openAIChatService.generateText(url, params);
            System.out.println(generatedText);


            // 测试 generateText 方法，包含聊天ID和新消息
            String chatId = "chat123";
            JSONObject newMessage = new JSONObject().put("role", "user").put("content", "What my last question?");
            String generatedTextWithChatId = openAIChatService.generateText(url, chatId, newMessage);
            System.out.println("Generated Text (With Chat ID): " + generatedTextWithChatId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}