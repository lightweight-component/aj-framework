package com.ajaxjs.rag.service.embedding;

import com.ajaxjs.rag.constant.Config;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestBaichuanEmbeddingService {
    @Test
    public void test() throws IOException {
        // 替换为您的API密钥
        String apiKey = Config.API_KEY;
        BaichuanEmbeddingService embeddingService = new BaichuanEmbeddingService(apiKey);

        // API的URL
        String url = "https://api.baichuan-ai.com/v1/embeddings";
        // 这里可以替换为您想要获取嵌入的文本
        String input = "百川大模型";
        double[] embedding = embeddingService.getEmbedding(url, input);
        // 打印嵌入向量
        for (double value : embedding)
            System.out.println(value);
    }
}
