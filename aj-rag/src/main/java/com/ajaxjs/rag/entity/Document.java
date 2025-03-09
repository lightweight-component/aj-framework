package com.ajaxjs.rag.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class Document {
    @JSONField(name = "user_id")
    String userId;
    @JSONField(name = "file_id")
    String fileId;
    @JSONField(name = "kb_id")
    String kbId;
    @JSONField(name = "chunk_id")
    Integer chunkId;
    @JSONField(name = "chunk_size")
    Integer chunkSize;
    @JSONField(name = "chunk_text")
    String chunkText;
    @JSONField(name = "text_emb")
    double[] textEmb;
    @JSONField(name = "clip_emb")
    double[] clipEmb;
    @JSONField(name = "doc_type")
    String docType;
    @JSONField(name = "version")
    String version;
    @JSONField(name = "author")
    String author;
    @JSONField(name = "created_time")
    Long createdTime;
    @JSONField(name = "modified_time")
    Long modifiedTime;
    @JSONField(name = "file_name")
    String fileName;
    @JSONField(name = "storage_path")
    String storagePath;
    @JSONField(name = "_score")
    Float score;

    public Document() {
    }

    public Document(String storagePath) {
        this.storagePath = storagePath;
    }
}