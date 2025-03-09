package com.ajaxjs.rag.entity;

import lombok.Data;

import java.util.List;
@Data
public class SearchOutput {
    public List<Document> documents;
    public Integer code;
    public String msg;
}
