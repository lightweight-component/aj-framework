package com.ajaxjs.rag.search;

import org.entity.Document;
import org.entity.SearchInput;
import org.entity.SearchOutput;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortStrategy {
    public static void dummySort(SearchInput searchInput, SearchOutput searchOutput){
        List<Document> documents = searchOutput.getDocuments();
        Collections.sort(documents, Comparator.comparingDouble(Document::getScore).reversed()); // 按score降序排序
    }
}