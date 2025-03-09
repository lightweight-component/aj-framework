package com.ajaxjs.rag.rag;

import com.ajaxjs.rag.entity.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestNaiveRAG {
    @Test
    public void test() {
        NaiveRAG naiveRAG = new NaiveRAG(new Document("./202X企业规划.pdf"),
                "简要总结这篇文章");
        try {
            naiveRAG
                    // 解析
                    .parsering()
                    // 分块
                    .chunking()
                    // 向量化
                    .embedding()
                    // 排序
                    .sorting()
                    // 大模型回复
                    .LLMChat();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(naiveRAG.getResponse());
    }
}
