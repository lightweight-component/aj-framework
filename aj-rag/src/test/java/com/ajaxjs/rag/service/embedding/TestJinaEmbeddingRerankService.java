package com.ajaxjs.rag.service.embedding;

import com.ajaxjs.rag.constant.Config;
import com.ajaxjs.rag.entity.Document;
import com.ajaxjs.rag.rag.NaiveRAG;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestJinaEmbeddingRerankService {
    @Test
    public void test() throws IOException {
        String apiKey = Config.Jina_API_KEY;   // 替换为您的API密钥
        JinaEmbeddingRerankService service = new JinaEmbeddingRerankService(apiKey);
        // API的URL
        String url = "https://api.jina.ai/v1/multi-vector";
        // 这里可以替换为您想要获取嵌入的文本数组
        String[] inputs = {
                "hello",
                "你好"
        };
        double[][] embeddings = service.getEmbeddings(url, inputs);
        // 打印嵌入向量
        System.out.println(embeddings[0][0]);
    }
}
