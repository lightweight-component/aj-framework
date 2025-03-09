package com.ajaxjs.rag.rag;

import com.ajaxjs.rag.chunk.FixedSizeSplitter;
import com.ajaxjs.rag.constant.Config;
import com.ajaxjs.rag.entity.Document;
import com.ajaxjs.rag.parser.FileParserFactory;
import com.ajaxjs.rag.service.LLM.OpenAIChatService;
import com.ajaxjs.rag.service.embedding.BaichuanEmbeddingService;
import com.ajaxjs.rag.utils.DistanceUtils;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdvancedRAG {
    private Document document;
    private List<Document> chunks;
    private String query;
    private String response;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public AdvancedRAG() {
    }

    public AdvancedRAG(Document document, String query) {
        this.document = document;
        this.query = query;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * 解析文档
     *
     * @return 当前对象，方便链式调用
     */
    public AdvancedRAG parsing() {
        String filePath = document.getStoragePath();
        if (!StringUtils.hasText(filePath))
            throw new IllegalArgumentException("Document storage path cannot be blank");

        String chunkText = FileParserFactory.easyParse(filePath);
        document.setChunkText(chunkText);

        return this;
    }

    /**
     * 分块处理
     *
     * @return 当前对象，方便链式调用
     */
    public AdvancedRAG chunking() {
        // 可以使用更复杂的分块策略，这里简单示例使用固定大小分块
        FixedSizeSplitter fixedSizeSplitter = new FixedSizeSplitter(512);
        List<String> stringList = fixedSizeSplitter.split(document.getChunkText());
        chunks = new ArrayList<>();

        for (String chunkText : stringList) {
            Document chunkDoc = new Document();
            chunkDoc.setChunkText(chunkText);
            chunks.add(chunkDoc);
        }

        return this;
    }

    /**
     * 嵌入处理
     *
     * @return 当前对象，方便链式调用
     * @throws IOException 可能的IO异常
     */
    public AdvancedRAG embedding() throws IOException {
        BaichuanEmbeddingService embeddingService = new BaichuanEmbeddingService(Config.API_KEY);
        // 获取查询的嵌入向量
        double[] queryEmbedding = embeddingService.getEmbedding(Config.EMBEDDING_API_URL, query);

        // 为每个文档块生成嵌入向量
        for (Document chunk : chunks) {
            double[] chunkEmbedding = embeddingService.getEmbedding(Config.EMBEDDING_API_URL, chunk.getChunkText());
            chunk.setTextEmb(chunkEmbedding);
        }
        return this;
    }

    /**
     * 排序处理
     *
     * @return 当前对象，方便链式调用
     * @throws IOException 可能的IO异常
     */
    public AdvancedRAG sorting() throws IOException {
        BaichuanEmbeddingService embeddingService = new BaichuanEmbeddingService(Config.API_KEY);
        // 获取查询的嵌入向量
        double[] queryEmbedding = embeddingService.getEmbedding(Config.EMBEDDING_API_URL, query);
        // 根据嵌入向量与查询的嵌入向量之间的距离对文档块进行排序
        chunks.sort(Comparator.comparingDouble(chunk -> DistanceUtils.squaredErrorDistance(queryEmbedding, chunk.getTextEmb())));

        return this;
    }

    /**
     * 高级筛选，例如过滤掉一些不相关的块
     *
     * @return 当前对象，方便链式调用
     */
    public AdvancedRAG advancedFiltering() {
        // 简单示例：过滤掉长度小于10的块
        chunks = chunks.stream()
                .filter(chunk -> chunk.getChunkText().length() > 10)
                .collect(Collectors.toList());

        return this;
    }

    /**
     * 大模型聊天
     *
     * @return 当前对象，方便链式调用
     */
    public AdvancedRAG LLMChat() {
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
                            new JSONObject().put("role", "user").put("content", query)
                    })
                    .put("temperature", 0.3)
                    .put("stream", false);

            // 调用大模型生成回复
            response = openAIChatService.generateText(url, params);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }
}