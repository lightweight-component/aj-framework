package com.ajaxjs.business.nlp;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import org.junit.jupiter.api.Test;

public class TestBase {
    @Test
    public void testStop() {
        Term term = new Term("的", Nature.ud);

        boolean b = TFIDF.shouldInclude(term);
        System.out.println(b);
    }
}
