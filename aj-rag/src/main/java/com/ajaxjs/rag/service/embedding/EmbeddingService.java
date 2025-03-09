package com.ajaxjs.rag.service.embedding;

import java.io.IOException;

public interface EmbeddingService {
    double[] getEmbedding(String url, String input) throws IOException;

    public double[][] getEmbeddings(String url, String[] inputs) throws IOException;
}
