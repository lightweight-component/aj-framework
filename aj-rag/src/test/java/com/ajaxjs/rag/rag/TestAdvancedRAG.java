package com.ajaxjs.rag.rag;

import com.ajaxjs.rag.entity.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestAdvancedRAG {
    @Test
    public void test() throws IOException {
        AdvancedRAG advancedRAG = new AdvancedRAG(new Document("./202X企业规划.pdf"),
                "简要总结这篇文章");

        advancedRAG
                // 解析
                .parsing()
                // 分块
                .chunking()
                // 向量化
                .embedding()
                // 排序
                .sorting()
                // 高级筛选
                .advancedFiltering()
                // 大模型回复
                .LLMChat();

        System.out.println(advancedRAG.getResponse());
    }
}
