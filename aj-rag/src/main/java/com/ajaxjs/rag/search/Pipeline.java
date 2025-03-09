package com.ajaxjs.rag.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import com.ajaxjs.rag.entity.SearchInput;
import com.ajaxjs.rag.entity.SearchOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline {
    private final static Logger logger = LoggerFactory.getLogger(Pipeline.class);

    static {
        try {
            ParserConfig.getGlobalInstance().setSafeMode(true);
            JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.WriteNonStringKeyAsString.getMask();
            JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();
        } catch (Throwable t) {
            logger.info(t.getMessage(), t);
        }
    }

    private SearchInput searchInput;
    private SearchOutput searchOutput;

    public void recall(SearchInput searchInput, SearchOutput searchOutput) {
        RecallStrategy.esRecall(searchInput, searchOutput);
    }


    public void recall() {
        RecallStrategy.esRecall(searchInput, searchOutput);
    }

    public void sort() {
        SortStrategy.dummySort(searchInput, searchOutput);
    }

    public void rerank(SearchInput searchInput, SearchOutput searchOutput) {
        RerankStrategy.JinaCobertRerank(searchInput, searchOutput);
    }

    public void rerank() {
        RerankStrategy.JinaCobertRerank(searchInput, searchOutput);
    }

    public SearchOutput getDefaultResult() {
        recall();// 召回
        sort();// 排序
        rerank(); // 重排

        return searchOutput;
    }
}
