package com.ajaxjs.rag.rag;

import com.ajaxjs.rag.entity.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestModularRAG {
    @Test
    public void test() {
        ModularRAG modularRAG = new ModularRAG(
                new Document("./202X企业规划.pdf"),
                "简要总结这篇文章");
        try {
            modularRAG
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
                    // 对筛选后的块重新排序
                    .reSortingFilteredChunks()
                    // 大模型回复
                    .LLMChat()
                    // 后处理
                    .postProcessing();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(modularRAG.getResponse());
    }
}
