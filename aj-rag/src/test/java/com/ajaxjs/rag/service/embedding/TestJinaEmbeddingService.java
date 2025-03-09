package com.ajaxjs.rag.service.embedding;

import com.ajaxjs.rag.constant.Config;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestJinaEmbeddingService {
    @Test
    public void test() throws IOException {
        // 替换为您的API密钥
        String apiKey = Config.Jina_API_KEY;
        JinaEmbeddingService embeddingService = new JinaEmbeddingService(apiKey);

        // API的URL
        String url = "https://api.jina.ai/v1/embeddings";
        // 这里可以替换为您想要获取嵌入的文本
        String input = "您的查询可以是中文";
        double[] embedding = embeddingService.getEmbedding(url, input);
        // 打印嵌入向量
        for (double value : embedding)
            System.out.println(value);
    }
}
