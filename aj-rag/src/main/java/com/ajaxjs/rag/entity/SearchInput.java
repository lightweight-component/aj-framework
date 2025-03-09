package com.ajaxjs.rag.entity;

import lombok.Data;

@Data
public class SearchInput {
    public User user;
    public KnowledgeBase knowledgeBase;
    public Document document;
}
