package com.ajaxjs.rag.service.LLM;

import com.ajaxjs.rag.constant.Config;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class TestLLM {
    @Test
    public void testOllamaChatService() throws Exception {
        OllamaChatService service = new OllamaChatService();
        String response = service.generateChatCompletion("llama3.2", "why is the sky blue?");
        System.out.println(response);
    }

    @Test
    public void testOpenAIChatService() throws Exception {
        // 替换为您的API密钥
        String apiKey = Config.API_KEY;
        // 使用百川Baichuan3-Turbo模型
        String model = Config.LLM_MODEL;
        // API的URL
        String url = Config.LLM_URL;
        OpenAIChatService openAIChatService = new OpenAIChatService(apiKey);

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
    }
}
