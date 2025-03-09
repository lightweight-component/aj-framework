package com.ajaxjs.rag.search;

import com.ajaxjs.rag.entity.Document;
import com.ajaxjs.rag.entity.SearchInput;
import com.ajaxjs.rag.entity.SearchOutput;
import com.ajaxjs.rag.service.db.ESClient;

import java.util.List;

public class RecallStrategy {
    public static void esRecall(SearchInput searchInput, SearchOutput searchOutput) {
        ESClient esClient = ESClient.getInstance();
        Document queryDoc = searchInput.getDocument();
        List<Document> chunks = esClient.searchChunk(queryDoc, 1, 100);
        searchOutput.setDocuments(chunks);
    }
}
